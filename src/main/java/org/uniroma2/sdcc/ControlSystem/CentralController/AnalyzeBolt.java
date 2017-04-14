package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
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

        // retrieve traffic data from memory - if available
        String json_trafficDataList;
        List<TrafficData> trafficDataList;
        try {
            json_trafficDataList = (String) memcachedClient.get("traffic_list");
            trafficDataList = gson.fromJson(json_trafficDataList, listTypeTraffic);
        } catch (Exception e) {
            trafficDataList = new ArrayList<>();
        }

        // retrieve parking data from memory - if available
        String json_parkingDataList;
        List<ParkingData> parkingDataList;
        try {
            json_parkingDataList = (String) memcachedClient.get("parking_list");
            parkingDataList = gson.fromJson(json_parkingDataList, listTypeParking);
        } catch (Exception e) {
            parkingDataList = new ArrayList<>();
        }

        // emit
        emitAnalyzedDataTuple(tuple, trafficDataList, parkingDataList);
        collector.ack(tuple);
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


        Integer id = (Integer) tuple.getValueByField(StreetLampMessage.ID);
        Address address = (Address) tuple.getValueByField(StreetLampMessage.ADDRESS);
        Integer cellID = (Integer) tuple.getValueByField(StreetLampMessage.CELL);
        Boolean on = (Boolean) tuple.getValueByField(StreetLampMessage.ON);
        String model = (String) tuple.getValueByField(StreetLampMessage.LAMP_MODEL);
        Float consumption = (Float) tuple.getValueByField(StreetLampMessage.CONSUMPTION);
        Float intensity = (Float) tuple.getValueByField(StreetLampMessage.INTENSITY);
        Float naturalLightLevel = (Float) tuple.getValueByField(StreetLampMessage.NATURAL_LIGHT_LEVEL);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(StreetLampMessage.LIFETIME);
        Long timestamp = (Long) tuple.getValueByField(StreetLampMessage.TIMESTAMP);

        TrafficData trafficData;
        // check traffic availability
        if ((trafficData = getTrafficByStreet(totalTrafficData, address.getName())) == null)
            trafficData = new TrafficData(address.getName(), 0f);

        ParkingData parkingData;
        // check parking availability
        if ((parkingData = getParkingByCellID(totalParkingData, cellID)) == null)
            parkingData = new ParkingData(cellID, 0f);

        HashMap<MalfunctionType, Float> anomalies =
                (HashMap<MalfunctionType,Float>) tuple.getValueByField(StreetLampMessage.MALFUNCTIONS_TYPE);

        Float anomalyGap;   // final positive or negative value to optimize light intensity


        /*
         * Control on other anomalies cannot be resolved
         * (e.i. DAMAGE_BULB, NOT RESPONDING)
         */
        if (!lampDamaged(anomalies)) {


            if ( (anomalyGap = anomalies.get(MalfunctionType.WEATHER_LESS)) != null )
                if (!toIncreaseGap.equals(anomalyGap))
                    toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.WEATHER_LESS

            else if ( (anomalyGap = anomalies.get(MalfunctionType.WEATHER_MORE)) != null)
                if (!toDecreaseGap.equals(anomalyGap))
                    toDecreaseGap = Math.min(toDecreaseGap, anomalyGap);    // MalfunctionType.WEATHER_MORE


            if ( (anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS)) != null)
                if (!toDecreaseGap.equals(anomalyGap))
                    toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.LIGHT_INTENSITY_LESS

            else if ( (anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE)) != null)
                if (!toDecreaseGap.equals(anomalyGap))
                    toDecreaseGap = Math.min(toDecreaseGap, anomalyGap);    // MalfunctionType.LIGHT_INTENSITY_MORE


            Values values = new Values();
            values.add(id);
            values.add(address);
            values.add(cellID);
            values.add(model);
            values.add(consumption);
            values.add(intensity);
            values.add(lifetime);
            values.add(toIncreaseGap);
            values.add(toDecreaseGap);
            String json_trafficData = gson.toJson(trafficData);
            values.add(json_trafficData);
            String json_parkingData = gson.toJson(parkingData);
            values.add(json_parkingData);

            collector.emit(tuple, values);
        }
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
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.ID,
                AnomalyStreetLampMessage.ADDRESS, AnomalyStreetLampMessage.CELL,
                AnomalyStreetLampMessage.LAMP_MODEL, AnomalyStreetLampMessage.CONSUMPTION,
                AnomalyStreetLampMessage.INTENSITY, AnomalyStreetLampMessage.LIFETIME,
                Constant.GAP_TO_INCREASE, Constant.GAP_TO_DECREASE, Constant.TRAFFIC_BY_ADDRESS,
                Constant.PARKING_BY_CELLID));
    }
}
