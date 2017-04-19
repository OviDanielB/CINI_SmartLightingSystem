package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Config.ControlConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Bolt is the second component of the Control System's MAPE architecture.
 * Analyze and assembly data of incoming tuple from Monitor and in-memory data
 * about traffic level in the street where lamp is placed and parking occupation
 * in the cell where the lamp is placed.
 * Traffic level by streets and parking occupation are information obtained
 * from periodical request at a Traffic REST API and a Parking REST API.
 * If incoming tuple register the lamp anomalies NOT_RESPONDING or DAMAGE_BULB, they are
 * rejected because describe malfunctioning lamp that cannot be adapted anyway.
 * If both no anomalies have been noticed and no significant traffic level or cell
 * parking occupation percentage have been measured, tuple is rejected because no
 * adaptation is needed.
 */
public class AnalyzeBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;
    private Type listTypeTraffic;
    private Type listTypeParking;
    private MemcachedClient memcachedClient;
    private final static String MEMCACHED_SERVER = "localhost";
    private final static int MEMCACHED_PORT = 11211;
    private Float toIncreaseGap = 0f;           // positive max value to resolve the defecting anomalies
    private Float toDecreaseGap = 0f;           // negative min value to resolve the excess anomalies

    private Float TRAFFIC_TOLERANCE_DEFAULT = 20f;
    private Float PARKING_TOLERANCE_DEFAULT = 20f;
    private Float traffic_tolerance; // only above this value traffic level affect intensity adaptation
    private Float parking_tolerance; // only above this value parking occupation affect intensity adaptation


    /**
     * Bolt initialization
     *
     * @param map map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();
        this.listTypeTraffic = new TypeToken<ArrayList<TrafficData>>() {
        }.getType();
        this.listTypeParking = new TypeToken<ArrayList<ParkingData>>() {
        }.getType();

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER,
                    MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        config();
    }

    /**
     * Configuration.
     */
    private void config() {

        Config config = new Config();
        config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {
            ControlConfig controlConfig = yamlConfigRunner.getConfiguration()
                    .getControlThresholds();

            this.traffic_tolerance = controlConfig.getTraffic_tolerance();
            this.parking_tolerance = controlConfig.getParking_tolerance();

        } catch (IOException e) {
            this.traffic_tolerance = TRAFFIC_TOLERANCE_DEFAULT;
            this.parking_tolerance = PARKING_TOLERANCE_DEFAULT;
        }
    }


    /*
    *   Monitoring Results Data Format:
    *
    *   1   id                          32 bit street-lamp identifier
    *   2   address                     street-lamp location (es Via/Piazza - km/civico -)
    *   3   cellID                      32 bit lamp cell park identifier
    *   4   on ( state on/off )         state
    *   5   consumption                 32 bit value representing energy consumption in Watt
    *   6   intensity                   percentage of the maximum intensity
    *   7   lifetime                    date
    *   8   naturalLightLevel           level of the measured natural light intensity
    *   9   list of anomalies           couples composed by kind of anomaly monitored [none, weather, damaged_bulb,
    *                                   light_intensity_anomaly, not_responding] and amount of anomaly
    */

    /**
     * AnalyzeBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        // get traffic data from memory (if available)
        List<TrafficData> trafficDataList = getTrafficDataInMemory();
        // get parking data from memory (if available)
        List<ParkingData> parkingDataList = getParkingDataInMemory();

        // emit
        emitAnalyzedDataTuple(tuple, trafficDataList, parkingDataList);

        collector.ack(tuple);
    }

    /**
     * Get Parking Data from memory (if available).
     *
     * @return list of parking data referring to all streets
     */
    private List<ParkingData> getParkingDataInMemory() {

        String json_parkingDataList;
        try {
            json_parkingDataList = (String) memcachedClient.get("parking_list");
            return gson.fromJson(json_parkingDataList, listTypeParking);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get Traffic Data from memory (if available).
     *
     * @return list of traffic data referring to all streets
     */
    private List<TrafficData> getTrafficDataInMemory() {

        String json_trafficDataList;
        try {
            json_trafficDataList = (String) memcachedClient.get("traffic_list");
            return gson.fromJson(json_trafficDataList, listTypeTraffic);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * Check and emit only tuple referring to lamps to adapt, grouping
     * information about anomalies of the lamp analyzed and measured street
     * traffic data and cell parking occupation.
     * Tuple with a DAMAGED_BULB or NOT_RESPONDING type of anomaly are rejected,
     * because they are broken and they cannot be contacted to adaptation.
     *
     * @param tuple                    received from spout tuple
     * @param totalTrafficData         list of all streets with their traffic level percentage
     * @param totalParkingData         list of all cellIDs with their parking occupation percentage
     */
    private void emitAnalyzedDataTuple(Tuple tuple, List<TrafficData> totalTrafficData, List<ParkingData> totalParkingData) {

        Integer id = (Integer) tuple.getValueByField(Constants.ID);
        Address address = (Address) tuple.getValueByField(Constants.ADDRESS);
        Integer cellID = (Integer) tuple.getValueByField(Constants.CELL);
        Float intensity = (Float) tuple.getValueByField(Constants.INTENSITY);

        TrafficData trafficData;
        // check traffic availability
        if ((trafficData = getTrafficByStreet(totalTrafficData, address.getName())) == null)
            trafficData = new TrafficData(address.getName(), 0f);

        ParkingData parkingData;
        // check parking availability
        if ((parkingData = getParkingByCellID(totalParkingData, cellID)) == null)
            parkingData = new ParkingData(cellID, address.getName(), 0f);

        HashMap<MalfunctionType, Float> anomalies =
                (HashMap<MalfunctionType,Float>) tuple.getValueByField(Constants.MALFUNCTIONS_TYPE);


//      Control on other anomalies cannot be resolved
//      (e.i. DAMAGE_BULB, NOT RESPONDING)
//      Change is not required if no anomalous lamp and no significant
//      traffic level or cell parking occupation percentage have been measured
        if (!lampDamaged(anomalies)) {

            computeGapToSolve(anomalies);

            if (changeRequired(trafficData, parkingData)) {

                Values values = new Values();
                values.add(id);
                values.add(intensity);
                values.add(toIncreaseGap);
                values.add(toDecreaseGap);
                String json_trafficData = gson.toJson(trafficData);
                values.add(json_trafficData);
                String json_parkingData = gson.toJson(parkingData);
                values.add(json_parkingData);

                collector.emit(tuple, values);
            }
        }
    }

    /**
     * Check if data observed require an adapting operation.
     *
     * @param trafficData traffic level
     * @param parkingData cell occupation
     * @return true if lamp has to be adapted
     */
    private boolean changeRequired(TrafficData trafficData, ParkingData parkingData) {
        return !toIncreaseGap.equals(0f)
                || !toDecreaseGap.equals(0f)
                || trafficData.getCongestionPercentage() > traffic_tolerance
                || parkingData.getOccupationPercentage() > parking_tolerance;
    }

    /**
     * Compute amount of intensity to increase/decrease to solve gap anomaly measured.
     *
     * @param anomalies couples (anomaly, gap) measured
     */
    private void computeGapToSolve(HashMap<MalfunctionType,Float> anomalies) {

        Float anomalyGap;

        if ((anomalyGap = anomalies.get(MalfunctionType.WEATHER_LESS)) != null)
            toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.WEATHER_LESS

        if ((anomalyGap = anomalies.get(MalfunctionType.WEATHER_MORE)) != null)
            toDecreaseGap = Math.min(toDecreaseGap, -anomalyGap);    // MalfunctionType.WEATHER_MORE

        if ((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS)) != null)
            toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.LIGHT_INTENSITY_LESS

        if ((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE)) != null)
            toDecreaseGap = Math.min(toDecreaseGap, -anomalyGap);    // MalfunctionType.LIGHT_INTENSITY_MORE
    }

    /**
     * Check if the lamp has DAMAGED_BULB or NOT_RESPONDING type of anomaly.
     *
     * @param anomalies map between malfunction type and difference gap from correct value
     * @return true is lamp damaged
     */
    private boolean lampDamaged(HashMap<MalfunctionType, Float> anomalies) {

        return anomalies.get(MalfunctionType.NOT_RESPONDING) != null
                || anomalies.get(MalfunctionType.DAMAGED_BULB) != null;
    }

    /**
     * Select traffic data values referring to the street specified
     * among all streets traffic data.
     *
     * @param totalTrafficData list of all streets traffic data values
     * @param name of the street to query traffic data for
     * @return traffic data selected by street; null if not available
     */
    private TrafficData getTrafficByStreet(List<TrafficData> totalTrafficData, String name) {

        if (totalTrafficData != null) {
            for (TrafficData trafficData : totalTrafficData) {
                if (trafficData.getStreet().equals(name)) {
                    return trafficData;
                }
            }
        }
        return null;
    }

    /**
     * Select parking data values referring to the cellID specified
     * among all cellIDs parking data.
     *
     * @param totalParkingData list of all cellIDs parking data values
     * @param cellID id of the cell to query parking data for
     * @return parking data selected by cellID; null if not available
     */
    private ParkingData getParkingByCellID(List<ParkingData> totalParkingData, int cellID) {

        if (cellID != -1 && totalParkingData != null) {
            for (ParkingData parkingData : totalParkingData) {
                if (parkingData.getCellID() == cellID) {
                    return parkingData;
                }
            }
        }
        return null;
    }

    /**
     * Declare name of the output tuple fields to sent to PlanBolt.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.ID,
                Constants.INTENSITY,
                Constants.GAP_TO_INCREASE,
                Constants.GAP_TO_DECREASE,
                Constants.TRAFFIC_BY_ADDRESS,
                Constants.PARKING_BY_CELLID));
    }
}
