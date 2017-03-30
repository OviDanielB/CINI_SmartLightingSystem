package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Bolt.FilteringByLifetimeBolt;
import org.uniroma2.sdcc.Bolt.GlobalRankBolt;
import org.uniroma2.sdcc.Bolt.PartialRankBolt;
import org.uniroma2.sdcc.Model.StreetLampMessage;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

/**
 * This Topology processes data generating real-time information about
 * number, id, position (address) of lamps which have been replaced for
 * more than LIFETIME_THRESHOLD days ago.
 *
 **/


public class RankingOldestLampsTopology {

    private static String QUERY_2_TOPOLOGY = "query2";
    private static String RABBIT_SPOUT = "rabbitSpout";
    private static String FILTER_BOLT = "filterBolt";
    private static String FILTER_BY_LIFETIME_BOLT = "filterByLifetimeBolt";
    private static String PARTIAL_RANK_BOLT = "partRankBolt";
    private static String GLOBAL_RANK_BOLT = "globalRankBolt";
    /*  TODO da mettere come parametro di configurazione */
    private static int oldest_k = 10;

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();

        /* Lamps' data source  */
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout());

        /* Check of format correctness of received tuples   */
        builder.setBolt(FILTER_BOLT, new FilteringBolt(),3)
                .shuffleGrouping(RABBIT_SPOUT);

        /* Filtering from lamps which have been replace within LIFETIME_THRESHOLD days from now */
        builder.setBolt(FILTER_BY_LIFETIME_BOLT, new FilteringByLifetimeBolt(),3)
                .shuffleGrouping(FILTER_BOLT);

        /* Data grouped by "lifetime" field and partially sorted by it   */
        builder.setBolt(PARTIAL_RANK_BOLT, new PartialRankBolt(oldest_k),5)
                .shuffleGrouping(FILTER_BY_LIFETIME_BOLT);

        /* Global ranking f the first K lamps with greater "lifetime" */
        builder.setBolt(GLOBAL_RANK_BOLT, new GlobalRankBolt(oldest_k))
                .allGrouping(PARTIAL_RANK_BOLT);


        /* LOCAL MODE */
//        LocalCluster cluster = new LocalCluster();
//        cluster.submitTopology(QUERY_2_TOPOLOGY, config, builder.createTopology());
//
//        Thread.sleep(600000);
//
//        cluster.killTopology(QUERY_2_TOPOLOGY);
//        cluster.shutdown();


        StormSubmitter.submitTopology(QUERY_2_TOPOLOGY,config,builder.createTopology());

    }
}
