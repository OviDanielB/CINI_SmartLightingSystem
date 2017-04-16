package org.uniroma2.sdcc;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Bolt.MalfunctionCheckBolt;
import org.uniroma2.sdcc.Bolt.NotRespondingLampBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.AnalyzeBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.ExecuteBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.PlanBolt;
import org.uniroma2.sdcc.Model.StreetLampMessage;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

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

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        //config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout());

        builder.setBolt(FILTER_BOLT, new FilteringBolt())
                .shuffleGrouping(RABBIT_SPOUT);

        builder.setBolt(MALFUNCTION_CHECK_BOLT, new MalfunctionCheckBolt())
                .fieldsGrouping(FILTER_BOLT,new Fields(Constants.ADDRESS));

        builder.setBolt(NOT_RESPONDING_LAMP_BOLT,new NotRespondingLampBolt())
                .fieldsGrouping(MALFUNCTION_CHECK_BOLT,new Fields(Constants.ID));

        builder.setBolt(ANALYZE_CONTROL_BOLT,new AnalyzeBolt())
                .fieldsGrouping(NOT_RESPONDING_LAMP_BOLT,new Fields(Constants.ADDRESS));

        builder.setBolt(PLAN_CONTROL_BOLT,new PlanBolt())
                .fieldsGrouping(ANALYZE_CONTROL_BOLT,new Fields(Constants.ID));

        builder.setBolt(EXECUTE_CONTROL_BOLT,new ExecuteBolt())
                .fieldsGrouping(PLAN_CONTROL_BOLT,new Fields(Constants.ID));


        /* LOCAL MODE */
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(QUERY_1_TOPOLOGY, config, builder.createTopology());

        Thread.sleep(600000);

        cluster.killTopology(QUERY_1_TOPOLOGY);
        cluster.shutdown();




        //StormSubmitter.submitTopology(QUERY_1_TOPOLOGY,config,builder.createTopology());

    }

}
