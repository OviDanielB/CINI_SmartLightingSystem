package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
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
    private Float toEncreaseGap;
    private Float toDecreaseGap;

    private Type listType;



    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;

    public AnalyzeBolt() {
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();
        this.listType = new TypeToken<ArrayList<TrafficData>>(){}.getType();

        try {
            memcachedClient =
                    new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
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

        String json = (String) tuple.getValueByField(Constant.JSON_STRING);

        AnomalyStreetLampMessage anomalyStreetLampMessage;
        try {
            /* JSON to Java object, read it from a Json String. */
            anomalyStreetLampMessage =
                    gson.fromJson(json, AnomalyStreetLampMessage.class);

        } catch (JsonParseException e) {
            /* wrong json format */
            e.printStackTrace();
            collector.ack(tuple);
            return;
        }

        String json_trafficDataList =  (String) memcachedClient.get("traffic");
        List<TrafficData> trafficDataList = gson.fromJson(json_trafficDataList, listType);

        emitAnalyzedDataTuple(tuple, anomalyStreetLampMessage, trafficDataList);

        collector.ack(tuple);
    }



    /**
     * Check and emit only tuple referring to lamps to adapt
     *
     * @param tuple             received from spout tuple
     * @param anomalyStreetLampMessage parsed from tuple
     * @param totalTrafficData list of all streets with their traffic percentage
     */
    private void emitAnalyzedDataTuple(
            Tuple tuple, AnomalyStreetLampMessage anomalyStreetLampMessage,
            List<TrafficData> totalTrafficData) {

        if (validAnomalyStreetLampFormat(anomalyStreetLampMessage)) {

            StreetLamp lamp = anomalyStreetLampMessage.getStreetLamp();

            Integer id = lamp.getID();
            org.uniroma2.sdcc.Model.Address address = lamp.getAddress();
//            Boolean on = lamp.isOn();
            String model = lamp.getLampModel().toString();
            Float consumption = lamp.getConsumption();
            Float intensity = lamp.getLightIntensity();
            LocalDateTime lifetime = lamp.getLifetime();
//            Float naturalLightLevel = anomalyStreetLampMessage.getNaturalLightLevel();
            Long timestamp = anomalyStreetLampMessage.getTimestamp();

            TrafficData trafficData =
                    getTrafficByStreet(totalTrafficData, lamp.getAddress().getName());

            HashMap<MalfunctionType, Float> anomalies =
                    anomalyStreetLampMessage.getAnomalies();

            Float anomalyGap;

            if (!(anomalyGap = anomalies.get(MalfunctionType.NONE)).equals(0f)) {
                // MalfunctionType.NONE
                // gap maintained at 0, because if no anomaly
                // there's no gap from correct value to modify
                // lamp not-active cannot be adapted
//                collector.ack(tuple);
//                return;
//                gap = Math.max(Math.abs(gap), Math.abs(anomalyGap));
//                adaptedIntensity = adaptByTrafficLevel(lamp, trafficData, gap);
            }
            if (!toEncreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.WEATHER_LESS)))) {
                // MalfunctionType.WEATHER_LESS
                toEncreaseGap = Math.max(toEncreaseGap, anomalyGap);
//                adaptedIntensity = adaptByWeather(lamp, trafficData, gap);
            } else if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.WEATHER_MORE)))) {
                // MalfunctionType.WEATHER_MORE
                toDecreaseGap = Math.max(toDecreaseGap, anomalyGap);
//                adaptedIntensity = adaptByWeather(lamp, trafficData, gap);
            }
            if (!toEncreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.DAMAGED_BULB)))) {
                // MalfunctionType.DAMAGED_BULB
                // gap maintained at 0, because if the bulb is damaged it can't be
                // set to a correct value
                // lamp not-active cannot be adapted
//                collector.ack(tuple);
//                return;
            }
            if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS)))) {
                // MalfunctionType.LIGHT_INTENSITY_LESS
                toEncreaseGap = Math.max(toEncreaseGap, anomalyGap);
//                adaptedIntensity = adaptByNaturalLightLevel(lamp, trafficData);
            } else if (!toDecreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE)))) {
                // MalfunctionType.LIGHT_INTENSITY_MORE
                toDecreaseGap = Math.max(toDecreaseGap, anomalyGap);
//                adaptedIntensity = adaptByNaturalLightLevel(lamp, trafficData);
            }
            if (!toEncreaseGap.equals((anomalyGap = anomalies.get(MalfunctionType.NOT_RESPONDING)))) {
                // MalfunctionType.NOT_RESPONDING
                // gap maintained at 0, because if the bulb is damaged it can't be
                // set to a correct value
                // lamp not-active cannot be adapted
//                collector.ack(tuple);
//                return;
            }

            Values values = new Values();
            values.add(id);
            values.add(address);
//            values.add(on);
            values.add(model);
            values.add(consumption);
            values.add(intensity);
            values.add(lifetime);
//            values.add(naturalLightLevel);
            values.add(timestamp);
            values.add(toEncreaseGap);
            values.add(toDecreaseGap);

            String json_trafficData = gson.toJson(trafficData);
            values.add(json_trafficData);

            //System.out.println("[CINI] FILTERING : " + values.toString());

            collector.emit(tuple, values);
        }
        // if deformed tuple it is rejected
    }

    private TrafficData getTrafficByStreet(List<TrafficData> totalTrafficData, String name) {

        for (TrafficData trafficData : totalTrafficData) {
            if (trafficData.getStreet().equals(name)) {
                return trafficData;
            }
        }
        return null;
    }


    /**
     * the street light should have a valid format
     * ex: intensity and naturalLight level should be percentages,
     * timestamp should not be too far in the past, etc
     *
     * @param anomalyStreetLamp needed validation
     * @return true if valid, false otherwise
     */
    private boolean validAnomalyStreetLampFormat(AnomalyStreetLampMessage anomalyStreetLamp) {
        // TODO check if 1st field is ID,2nd is an address, etc
        return true;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.ID,
                AnomalyStreetLampMessage.ADDRESS, AnomalyStreetLampMessage.LAMP_MODEL,
                AnomalyStreetLampMessage.CONSUMPTION, AnomalyStreetLampMessage.INTENSITY,
                AnomalyStreetLampMessage.LIFETIME, AnomalyStreetLampMessage.TIMESTAMP,
                Constant.GAP_TO_ENCREASE, Constant.GAP_TO_DECREASE,
                Constant.TRAFFIC_BY_ADDRESS));

    }
}
