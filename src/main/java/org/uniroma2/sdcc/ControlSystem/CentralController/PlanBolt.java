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
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Model.TrafficData;

import java.time.LocalDateTime;
import java.util.Map;

public class PlanBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;

    private Float gap;                                      // final computed gap to resolve anomalies
    private static final Float TRAFFIC_THRESHOLD = 0.2f;    // under this value traffic is considered not relevant

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();
    }

    @Override
    public void execute(Tuple tuple) {

        Integer id = tuple.getIntegerByField(AnomalyStreetLampMessage.ID);
        Address address = (Address) tuple.getValueByField(AnomalyStreetLampMessage.ADDRESS);
        String model = tuple.getStringByField(AnomalyStreetLampMessage.LAMP_MODEL);
        Float consumption = tuple.getFloatByField(AnomalyStreetLampMessage.CONSUMPTION);
        Float intensity = tuple.getFloatByField(AnomalyStreetLampMessage.INTENSITY);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(AnomalyStreetLampMessage.LIFETIME);
        Float toEncreaseGap = tuple.getFloatByField(Constant.GAP_TO_ENCREASE);
        Float toDecreaseGap = tuple.getFloatByField(Constant.GAP_TO_DECREASE);

        TrafficData traffic = gson.fromJson(tuple.getStringByField(Constant.TRAFFIC_BY_ADDRESS),
                TrafficData.class);


        adaptByTrafficLevelAndAnomalies(toEncreaseGap, toDecreaseGap, intensity, traffic);

        // if needed adaptation, send tuple to Execute adaptation of intensity
        if (gap != 0) {

            Float adapted_intensity = intensity + gap;

            Values values = new Values();
            values.add(id);
            values.add(address);
            values.add(model);
            values.add(consumption);
            values.add(lifetime);
            values.add(adapted_intensity);

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
                Constant.ADAPTED_INTENSITY));
    }


    private void adaptByTrafficLevelAndAnomalies(Float toEncreaseGap, Float toDecreaseGap,
                                                 Float current_instensity, TrafficData trafficData) {

        Float traffic = trafficData.getCongestionPercentage();

        /*
         *  If there are both positive and negative luminosity gaps
         *  of distance from correct value, the positive one is preferred
         */
        if (toEncreaseGap.equals(0f)) {

            // traffic level relevant just above a threshold
            if ((traffic - 0f) > TRAFFIC_THRESHOLD) {
                // to encrease intensity of traffic level % of current intensity
                gap = traffic * (1 - current_instensity);
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
                gap += traffic * (1 - current_instensity);
            }
        }
    }
}
