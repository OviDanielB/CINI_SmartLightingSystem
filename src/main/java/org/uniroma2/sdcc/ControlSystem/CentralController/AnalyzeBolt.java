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

public class AnalyzeBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;
    private MemcachedClient memcachedClient;
    private Float toEncreaseGap = 0f;           // positive max value to resolve the defecting anomalies
    private Float toDecreaseGap = 0f;           // negative min value to resolve the excess anomalies

    private Type listType;


    private final static String MEMCACHED_SERVER = "localhost";
    private final static int MEMCACHED_PORT = 11211;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();
        this.listType = new TypeToken<ArrayList<TrafficData>>() {
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
    *   3   on ( state on/off )         state
    *   4   consumption                 32 bit value representing energy consumption in Watt
    *   5   intensity                   percentage of the maximum intensity
    *   6   lifetime                    date
    *   7   naturalLightLevel           level of the measured natural light intensity
    *   8   list of anomalies           couples composed by kind of anomaly monitored [none, weather, damaged_bulb,
    *                                   light_intensity_anomaly, not_responding] and amount of anomaly
    *   9   counter                     counter of non-responding lamps
    *   10  timestamp                   32 bit value
    */

    @Override
    public void execute(Tuple tuple) {

        // retrieve AnomalyStreetLampMessage from incoming tuple
        String json = (String) tuple.getValueByField(AnomalyStreetLampMessage.STREET_LAMP_MSG);
        AnomalyStreetLampMessage anomalyStreetLampMessage = gson.fromJson(json, AnomalyStreetLampMessage.class);

        // retrieve traffic data
        String json_trafficDataList = (String) memcachedClient.get("traffic");
        List<TrafficData> trafficDataList = gson.fromJson(json_trafficDataList, listType);

        // emit
        emitAnalyzedDataTuple(tuple, anomalyStreetLampMessage, trafficDataList);
        collector.ack(tuple);
    }


    /**
     * Check and emit only tuple referring to lamps to adapt
     *
     * @param tuple                    received from spout tuple
     * @param anomalyStreetLampMessage parsed from tuple
     * @param totalTrafficData         list of all streets with their traffic percentage
     */
    private void emitAnalyzedDataTuple(Tuple tuple, AnomalyStreetLampMessage anomalyStreetLampMessage,
                                       List<TrafficData> totalTrafficData) {


        StreetLamp lamp = anomalyStreetLampMessage.getStreetLamp();

        Integer id = lamp.getID();
        Address address = lamp.getAddress();
        String model = lamp.getLampModel().toString();
        Float consumption = lamp.getConsumption();
        Float intensity = lamp.getLightIntensity();
        LocalDateTime lifetime = lamp.getLifetime();

        TrafficData trafficData = getTrafficByStreet(totalTrafficData, lamp.getAddress()
                .getName());

        HashMap<MalfunctionType, Float> anomalies = anomalyStreetLampMessage.getAnomalies();

        Float anomalyGap;   // final positive or negative value to optimize light intensity


        /*
         * Control on other anomalies cannot be resolved
         * (e.i. DAMAGE_BULB, NOT RESPONDING)
         */
        if (!toEncreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.WEATHER_LESS))))
            toEncreaseGap = Math.max(toEncreaseGap, -anomalyGap);   // MalfunctionType.WEATHER_LESS

        else if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.WEATHER_MORE))))
            toDecreaseGap = Math.min(toDecreaseGap, anomalyGap);    // MalfunctionType.WEATHER_MORE


        if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS))))
            toEncreaseGap = Math.max(toEncreaseGap, -anomalyGap);   // MalfunctionType.LIGHT_INTENSITY_LESS

        else if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE))))
            toDecreaseGap = Math.min(toDecreaseGap, anomalyGap);    // MalfunctionType.LIGHT_INTENSITY_MORE


        Values values = new Values();
        values.add(id);
        values.add(address);
        values.add(model);
        values.add(consumption);
        values.add(intensity);
        values.add(lifetime);
        values.add(toEncreaseGap);
        values.add(toDecreaseGap);

        String json_trafficData = gson.toJson(trafficData);
        values.add(json_trafficData);
        collector.emit(tuple, values);
    }

    private TrafficData getTrafficByStreet(List<TrafficData> totalTrafficData, String name) {

        for (TrafficData trafficData : totalTrafficData) {
            if (trafficData.getStreet().equals(name)) {
                return trafficData;
            }
        }
        return null;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.ID,
                AnomalyStreetLampMessage.ADDRESS, AnomalyStreetLampMessage.LAMP_MODEL,
                AnomalyStreetLampMessage.CONSUMPTION, AnomalyStreetLampMessage.INTENSITY,
                AnomalyStreetLampMessage.LIFETIME, Constant.GAP_TO_ENCREASE,
                Constant.GAP_TO_DECREASE, Constant.TRAFFIC_BY_ADDRESS));

    }
}
