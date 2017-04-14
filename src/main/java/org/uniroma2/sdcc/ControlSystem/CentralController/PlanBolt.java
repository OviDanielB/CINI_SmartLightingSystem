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
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.TrafficData;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * This Bolt is the third component of the Control System's MAPE architecture.
 * Retrieve incoming tuple from Analyze, calculating the total amount of lamp
 * intensity (if any) to increase/decrease how much it's necessary.
 * Adaptation is based on measured anomalies, resulting from Monitor operation
 * WEATHER_LESS, WEATHER_MORE, LIGHT_INTENSITY_ANOMALY_LESS, LIGHT_INTENSITY_ANOMALY_MORE
 * (if any) and on traffic level measured in the street where lamp is placed and
 * on parking availability measured in the cell where lamp is placed.
 * If incoming tuple register the lamp anomalies NOT_RESPONDING or DAMAGE_BULB, they are
 * rejected because describe malfunctioning lamp that cannot be adapted anyway.
 */
public class PlanBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Gson gson;

    private Float adapted_intensity; // final computed intensity to resolve anomalies


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
    }

    /**
     * PlanBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        // retrieve data from incoming tuple
        Integer id = tuple.getIntegerByField(AnomalyStreetLampMessage.ID);
        Address address = (Address) tuple.getValueByField(AnomalyStreetLampMessage.ADDRESS);
        Integer cellID = (Integer) tuple.getValueByField(AnomalyStreetLampMessage.CELL);
        String model = tuple.getStringByField(AnomalyStreetLampMessage.LAMP_MODEL);
        Float consumption = tuple.getFloatByField(AnomalyStreetLampMessage.CONSUMPTION);
        Float intensity = tuple.getFloatByField(AnomalyStreetLampMessage.INTENSITY);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(AnomalyStreetLampMessage.LIFETIME);
        Float toIncreaseGap = tuple.getFloatByField(Constant.GAP_TO_INCREASE);
        Float toDecreaseGap = tuple.getFloatByField(Constant.GAP_TO_DECREASE);

        TrafficData traffic = gson.fromJson(tuple.getStringByField(Constant.TRAFFIC_BY_ADDRESS),
                TrafficData.class);
        ParkingData parking = gson.fromJson(tuple.getStringByField(Constant.PARKING_BY_CELLID),
                ParkingData.class);

        adaptByTrafficLevelAndAnomalies(toIncreaseGap, toDecreaseGap, intensity, traffic, parking);

        // if needed adaptation, send tuple to Execute adaptation of intensity
        if ( !adapted_intensity.equals(intensity) ) {

            Values values = new Values();
            values.add(id);
            values.add(address);
            values.add(cellID);
            values.add(model);
            values.add(consumption);
            values.add(lifetime);
            values.add(adapted_intensity);

            collector.emit(tuple, values);
        }
        // if no adaptation needed rejected tuple
        collector.ack(tuple);
    }

    /**
     * Declare name of the output tuple fields to sent to ExecuteBolt.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.ID,
                AnomalyStreetLampMessage.ADDRESS, AnomalyStreetLampMessage.CELL,
                AnomalyStreetLampMessage.LAMP_MODEL, AnomalyStreetLampMessage.CONSUMPTION,
                AnomalyStreetLampMessage.LIFETIME, Constant.ADAPTED_INTENSITY));
    }


    /**
     * If no gap to increase/decrease current lamp intensity has been computed
     * as necessary because of measured anomalies, adaptation is based only on street
     * traffic and cell parking availability percentages (if greater than TRAFFIC_THRESHOLD
     * and PARKING_THRESHOLD).
     * If no gap to increase current lamp intensity has been computed and no percentages of
     * street traffic and cell parking availability are (respectively) greater than
     * TRAFFIC_THRESHOLD and PARKING_THRESHOLD, adaptation is based on gap to decrease current
     * lamp intensity because it results wasteful comparing to current condition.
     * Otherwise, adaptation is based on to-increase-gap, street traffic percentage (if greater
     * than TRAFFIC_THRESHOLD) and cell parking availability percentage (if greater than
     * PARKING_THRESHOLD).
     *
     * @param toIncreaseGap amount of gap to add to current lamp intensity
     * @param toDecreaseGap amount of gap to subtract to current lamp intensity
     * @param current_intensity current lamp luminosity
     * @param trafficData traffic data by street where the lamp is placed
     * @param parkingData parking data by cellID where the lamp is placed
     */
    private void adaptByTrafficLevelAndAnomalies(Float toIncreaseGap, Float toDecreaseGap,
                                                 Float current_intensity, TrafficData trafficData,
                                                 ParkingData parkingData) {

        Float traffic = trafficData.getCongestionPercentage(); // traffic level percentage
        Float parking = parkingData.getOccupationPercentage(); // parking occupation percentage

        /*
         *  If there are both positive and negative luminosity gaps
         *  of distance from correct value, the positive one is preferred
         */
        if (toIncreaseGap.equals(0f)) {

            adapted_intensity = current_intensity;

            // traffic level relevant just above a threshold
            if ((traffic - 0f) > Constant.TRAFFIC_THRESHOLD) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (1 - adapted_intensity);
            } else if ((parking - 0f) > Constant.PARKING_THRESHOLD) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (1 - adapted_intensity);
            } else {
                // if no positive gap measured and no relevant traffic or parking percentages
                // the negative gap is considered
                adapted_intensity = current_intensity + toDecreaseGap;
            }
        } else {

            adapted_intensity = current_intensity + toIncreaseGap;
            // traffic level relevant just above a threshold
            if ((traffic - 0f) > Constant.TRAFFIC_THRESHOLD) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (1 - adapted_intensity);
            } else if ((parking - 0f) > Constant.PARKING_THRESHOLD) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (1 - adapted_intensity);
            }
        }
    }
}
