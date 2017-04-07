package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Model.TrafficData;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class PlanBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;
    private Float gap;
    // under this value traffic is considered not relevant
    private static final Float TRAFFIC_THRESHOLD = 0.2f;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();
    }

    @Override
    public void execute(Tuple tuple) {
        /*
         * combina dati sul traffico e luce naturale per calcolare nuovo valore di intensità
         * sulla strada.
         */

        int id =                (int) tuple.getValueByField(AnomalyStreetLampMessage.ID);
        String address =        tuple.getValueByField(AnomalyStreetLampMessage.ADDRESS).toString();
        String model =          tuple.getValueByField(AnomalyStreetLampMessage.LAMP_MODEL).toString();
        Float consumption =     (Float) tuple.getValueByField(AnomalyStreetLampMessage.CONSUMPTION);
        Float intensity =       (Float) tuple.getValueByField(AnomalyStreetLampMessage.INTENSITY);
        LocalDateTime lifetime =(LocalDateTime) tuple.getValueByField(AnomalyStreetLampMessage.LIFETIME);
        Timestamp timestamp =   (Timestamp) tuple.getValueByField(AnomalyStreetLampMessage.TIMESTAMP);
        Float toEncreaseGap =   (Float) tuple.getValueByField(Constant.GAP_TO_ENCREASE);
        Float toDecreaseGap =   (Float) tuple.getValueByField(Constant.GAP_TO_DECREASE);
        TrafficData traffic =   gson.fromJson(
                (String) tuple.getValueByField(Constant.TRAFFIC_BY_ADDRESS), TrafficData.class);


        adaptByTrafficLevelAndAnomalies(toEncreaseGap, toDecreaseGap, intensity, traffic);

        // if needed adaptation, send tuple to Execute adaptation of intensity
        if (gap != 0) {

            Float adapted_intensity = intensity + gap;

            Values values = new Values();
            values.add(id);
            values.add(address);
            values.add(model);
            values.add(consumption);
//            values.add(intensity);
            values.add(lifetime);
            values.add(timestamp);
            values.add(adapted_intensity);

            //System.out.println("[CINI] FILTERING : " + values.toString());

            collector.emit(tuple, values);
        }
        // if no adaptation needed rejected tuple
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.ID,
                AnomalyStreetLampMessage.ADDRESS, AnomalyStreetLampMessage.LAMP_MODEL,
                AnomalyStreetLampMessage.CONSUMPTION, AnomalyStreetLampMessage.LIFETIME,
                AnomalyStreetLampMessage.TIMESTAMP, Constant.ADAPTED_INTENSITY));
    }


    private void adaptByTrafficLevelAndAnomalies(Float toEncreaseGap, Float toDecreaseGap,
                                     Float current_instensity, TrafficData trafficData) {

        Float traffic = trafficData.getCongestionPercentage();

        // if there are both positive and negative luminosity gaps
        // of distance from correct value, the positive one is preferred
        if (toEncreaseGap.equals(0f)) {

            // traffic level relevant just above a threshold
            if ((traffic - 0f) > TRAFFIC_THRESHOLD) {
                // to encrease intensity of traffic level % of current intensity
                gap = traffic*(1-current_instensity);
            } else {
                // if no positive gap measured and no relevant traffic level
                // the negative gap is considered
                gap = toDecreaseGap;
            }
        } else {
            gap = toEncreaseGap;
            // traffic level relevant just above a threshold
            if ((traffic - 0f) > TRAFFIC_THRESHOLD) {
                // to encrease intensity of traffic level % of current intensity
                gap += traffic*(1-current_instensity);
            }
        }
    }
}
