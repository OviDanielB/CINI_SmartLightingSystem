package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Bolt.MalfunctionCheckBolt;
import org.uniroma2.sdcc.Model.StreetLampMessage;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

public class AnomaliesDetectionTopology {

    private static String QUERY_1_TOPOLOGY = "query1";
    private static String RABBIT_SPOUT = "rabbitSpout";
    private static String FILTER_BOLT = "filterBolt";
    private static String MALFUNCTION_CHECK_BOLT = "malfCheckBolt";

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        //config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout());

        builder.setBolt(FILTER_BOLT, new FilteringBolt())
                .shuffleGrouping(RABBIT_SPOUT);

        builder.setBolt(MALFUNCTION_CHECK_BOLT, new MalfunctionCheckBolt())
                .fieldsGrouping(FILTER_BOLT,new Fields(StreetLampMessage.ADDRESS));



        /* LOCAL MODE */
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(QUERY_1_TOPOLOGY, config, builder.createTopology());

        Thread.sleep(600000);

        cluster.killTopology(QUERY_1_TOPOLOGY);
        cluster.shutdown();




        //StormSubmitter.submitTopology(QUERY_1_TOPOLOGY,config,builder.createTopology());

    }

}
