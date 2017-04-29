package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Bolt.MalfunctionCheckBolt;
import org.uniroma2.sdcc.Bolt.NotRespondingLampBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.AnalyzeBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.ExecuteBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.PlanBolt;
import org.uniroma2.sdcc.ControlSystem.ParkingSource;
import org.uniroma2.sdcc.ControlSystem.TrafficSource;
import org.uniroma2.sdcc.Model.StreetLampMessage;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;
import org.uniroma2.sdcc.Traffic.Traffic;

import java.util.Timer;

/**
 * This Topology analyzes input data monitoring real-time lamps
 * condition, detecting intensity percentage anomalies and malfunctions:
 * - consumption amount of a single lamp is too different from statistics
 *   along the same street
 * - state is off when it must be on
 * - intensity percentage too low or too high according to visibility, weather,
 *      time conditions
 * - a lamp sent a tuple too much time ago
 * It generates an alarm when a lamp is considered malfunctioning.
 * Control System features are elaborating lamp intensity value,
 * its monitored anomalies (if any), traffic congestion level and parking cell occupation
 * percentage (if available).
 * As extension of Monitoring Topology, the Control System follows a MAPE architecture:
 * - Monitor is performed by the anomalies monitoring topology
 * - Analyze assembles results data from Monitor, TrafficSource (in-memory data) and
 *      ParkingSource (in-memory data).
 * - Plan computes a new intensity value according to the measured anomalies, traffic and
 *      parking percentages
 * - Execute sends results of adaptation to Local Controller
 **/

public class AnomaliesDetectionTopology {

    private static String QUERY_1_TOPOLOGY = "query1";
    private static String RABBIT_SPOUT = "rabbitSpout";
    private static String FILTER_BOLT = "filterBolt";
    private static String MALFUNCTION_CHECK_BOLT = "malfCheckBolt";
    private static String NOT_RESPONDING_LAMP_BOLT = "weatherBolt";
    private static String ANALYZE_CONTROL_BOLT = "analyzeBolt";
    private static String PLAN_CONTROL_BOLT = "planBolt";
    private static String EXECUTE_CONTROL_BOLT = "executeBolt";

    private static Integer  UPDATE_PERIOD = 10000;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        //config.setNumWorkers(4);
        //config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout());

        builder.setBolt(FILTER_BOLT, new FilteringBolt())
                .setNumTasks(10)
                .shuffleGrouping(RABBIT_SPOUT);

        builder.setBolt(MALFUNCTION_CHECK_BOLT, new MalfunctionCheckBolt())
                .setNumTasks(8)
                .fieldsGrouping(FILTER_BOLT,new Fields(Constants.ADDRESS));

        builder.setBolt(NOT_RESPONDING_LAMP_BOLT,new NotRespondingLampBolt())
                .setNumTasks(5)
                .fieldsGrouping(MALFUNCTION_CHECK_BOLT,new Fields(Constants.ID));

        builder.setBolt(ANALYZE_CONTROL_BOLT,new AnalyzeBolt())
                .setNumTasks(16)
                .fieldsGrouping(NOT_RESPONDING_LAMP_BOLT,new Fields(Constants.ADDRESS));

        builder.setBolt(PLAN_CONTROL_BOLT,new PlanBolt())
                .setNumTasks(5)
                .fieldsGrouping(ANALYZE_CONTROL_BOLT,new Fields(Constants.ID));

        builder.setBolt(EXECUTE_CONTROL_BOLT,new ExecuteBolt())
                .setNumTasks(5)
                .fieldsGrouping(PLAN_CONTROL_BOLT,new Fields(Constants.ID));

        startTrafficSource();


        /* LOCAL MODE */


        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(QUERY_1_TOPOLOGY, config, builder.createTopology());

        Thread.sleep(600000);

        cluster.killTopology(QUERY_1_TOPOLOGY);
        cluster.shutdown();




        //StormSubmitter.submitTopology(QUERY_1_TOPOLOGY,config,builder.createTopology());

    }

    /**
     * start a periodic traffic and parking data
     * update from external source
     */
    private static void startTrafficSource() {
        Timer timerTraffic = new Timer();
        TrafficSource source = new TrafficSource();
        timerTraffic.schedule(source,0,UPDATE_PERIOD);

        Timer timerParking = new Timer();
        ParkingSource parkingSource = new ParkingSource();
        timerParking.schedule(parkingSource,0,UPDATE_PERIOD);
    }

}
