package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Utils.JSONConverter;

import java.util.Map;


/**
 * This Bolt is the third component of the Control System's MAPE architecture.
 * Retrieve incoming tuple from Analyze, calculating the total amount of lamp
 * intensity (if any) to increase/decrease how much it's necessary.
 * Adaptation is based on measured anomalies, resulting from Monitor operation
 * WEATHER_LESS, WEATHER_MORE, LIGHT_INTENSITY_ANOMALY_LESS, LIGHT_INTENSITY_ANOMALY_MORE
 * (if any) and on traffic level measured in the street where lamp is placed and
 * on parking availability measured in the cell where lamp is placed.
 */
public class PlanBolt extends BaseRichBolt {

    private OutputCollector collector;

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
    }


    /**
     * PlanBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        Float adapted_intensity =
                adaptByTrafficLevelAndAnomalies(tuple);

        // send tuple to Execute adaptation of intensity
        emitPlannedIntensity(tuple, adapted_intensity);

        // if no adaptation needed rejected tuple
        collector.ack(tuple);
    }

    /**
     * If intensity calculated using data about anomalies, traffic and parking
     * percentages is not equal to the current one, Analyze emit tuple with
     * adaptation values to ExecuteBolt.
     *
     * @param tuple received
     * @param adapted_intensity necessary
     */
    private void emitPlannedIntensity(Tuple tuple, Float adapted_intensity) {

        Integer id = tuple.getIntegerByField(Constants.ID);
        Float intensity = tuple.getFloatByField(Constants.INTENSITY);

        if (!intensity.equals(adapted_intensity)) {

            Values values = new Values();
            values.add(id);
            values.add(adapted_intensity);

            collector.emit(tuple,values);
        }
    }

    /**
     * Declare name of the output tuple fields to sent to ExecuteBolt.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.ID,
                Constants.ADAPTED_INTENSITY));
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
     * @param tuple received
     * @return computed adepted intensity value
     */
    protected Float adaptByTrafficLevelAndAnomalies(Tuple tuple) {

        // retrieve data from incoming tuple
        Float current_intensity = tuple.getFloatByField(Constants.INTENSITY);
        Float toIncreaseGap = tuple.getFloatByField(Constants.GAP_TO_INCREASE);
        Float toDecreaseGap = tuple.getFloatByField(Constants.GAP_TO_DECREASE);
        TrafficData trafficData = JSONConverter.toTrafficData(tuple.getStringByField(Constants.TRAFFIC_BY_ADDRESS));
        ParkingData parkingData = JSONConverter.toParkingData(tuple.getStringByField(Constants.PARKING_BY_CELLID));

        Float traffic = 0f;
        if (trafficData != null)
            traffic = trafficData.getCongestionPercentage(); // traffic level percentage

        Float parking = 0f;
        if (parkingData != null)
            parking = parkingData.getOccupationPercentage(); // parking occupation percentage

        Float adapted_intensity;
        /*
         *  If there are both positive and negative luminosity gaps
         *  of distance from correct value, the positive one is preferred
         */
        if (toIncreaseGap.equals(0f)) {

            adapted_intensity = current_intensity;

            if (!traffic.equals(0f)) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
            }
            if (!parking.equals(0f)) {
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
            } else {
                // if no positive gap measured and no relevant traffic or parking percentages
                // the negative gap is considered
                adapted_intensity = current_intensity + toDecreaseGap;
            }
        } else {

            adapted_intensity = current_intensity + toIncreaseGap;
            // traffic level relevant just above a threshold
            if (!traffic.equals(0f)) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
            }
            if (!parking.equals(0f)) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
            }
        }
        return adapted_intensity;
    }
}
