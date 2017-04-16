package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import org.apache.storm.Config;
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
import org.uniroma2.sdcc.Utils.Config.ControlConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;
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
        config();
    }

    /**
     * Configuration.
     */
    private void config() {

        Config config = new Config();
        config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner("./config/config.yml");

        try {
            ControlConfig controlConfig = yamlConfigRunner.getConfiguration()
                    .getControlConfig();

            this.traffic_tolerance = controlConfig.getTraffic_tolerance();
            this.parking_tolerance = controlConfig.getParking_tolerance();

        } catch (IOException e) {
            this.traffic_tolerance = TRAFFIC_TOLERANCE_DEFAULT;
            this.parking_tolerance = PARKING_TOLERANCE_DEFAULT;
        }
    }

    /**
     * PlanBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        // retrieve data from incoming tuple
        Integer id = tuple.getIntegerByField(Constants.ID);
        Float intensity = tuple.getFloatByField(Constants.INTENSITY);
        Float toIncreaseGap = tuple.getFloatByField(Constants.GAP_TO_INCREASE);
        Float toDecreaseGap = tuple.getFloatByField(Constants.GAP_TO_DECREASE);
        TrafficData traffic = gson.fromJson(tuple.getStringByField(Constants.TRAFFIC_BY_ADDRESS),
                TrafficData.class);
        ParkingData parking = gson.fromJson(tuple.getStringByField(Constants.PARKING_BY_CELLID),
                ParkingData.class);

        adaptByTrafficLevelAndAnomalies(toIncreaseGap, toDecreaseGap, intensity, traffic, parking);

        // if needed adaptation, send tuple to Execute adaptation of intensity
        if ( !adapted_intensity.equals(intensity) ) {

            Values values = new Values();
            values.add(id);
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
            if ((traffic - 0f) > traffic_tolerance) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
            }
            if ((parking - 0f) > parking_tolerance) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
            } else {
                // if no positive gap measured and no relevant traffic or parking percentages
                // the negative gap is considered
                adapted_intensity = current_intensity - toDecreaseGap;
            }
        } else {

            adapted_intensity = current_intensity + toIncreaseGap;
            // traffic level relevant just above a threshold
            if ((traffic - 0f) > traffic_tolerance) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
            }
            if ((parking - 0f) > parking_tolerance) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
            }
        }
    }
}
