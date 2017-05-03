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
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;
import org.uniroma2.sdcc.Utils.Config.RankingConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;

/**
 * This Topology processes data generating real-time information about
 * number, id, position (address) of lamps which have been replaced for
 * more than LIFETIME_THRESHOLD days ago, computing a ranking of the first
 * K lamps sorted from the oldest one to the newest one.
 **/


public class RankingOldestLampsTopology {

    private static String QUERY_2_TOPOLOGY = "query2";
    private static String RABBIT_SPOUT = "rabbitSpout";
    private static String FILTER_BOLT = "filterBolt";
    private static String FILTER_BY_LIFETIME_BOLT = "filterByLifetimeBolt";
    private static String PARTIAL_RANK_BOLT = "partRankBolt";
    private static String GLOBAL_RANK_BOLT = "globalRankBolt";
    private static int RANK_SIZE_DEFAULT = 10;
    private static int LIFETIME_THRESHOLD_DEFAULT = 7;

    public static void main(String[] args) throws Exception {

        int rank_size;
        int lifetime_threshold;

        Config config = new Config();
        config.setNumWorkers(4);
        config.setMessageTimeoutSecs(10);
        //config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {
            RankingConfig rankingConfig = yamlConfigRunner.getConfiguration()
                    .getRankingTopologyParams();

            rank_size = rankingConfig.getRank_size();
            lifetime_threshold = rankingConfig.getLifetime_minimum();

        } catch (IOException e) {
            rank_size = RANK_SIZE_DEFAULT;
            lifetime_threshold = LIFETIME_THRESHOLD_DEFAULT;
        }

        TopologyBuilder builder = new TopologyBuilder();

        /* Lamps' data source  */
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout(),2);

        /* Check of format correctness of received tuples   */
        builder.setBolt(FILTER_BOLT, new FilteringBolt(),3)
                .setNumTasks(6)
                .shuffleGrouping(RABBIT_SPOUT);

        /* Filtering from lamps which have been replace within LIFETIME_THRESHOLD days from now */
        builder.setBolt(FILTER_BY_LIFETIME_BOLT, new FilteringByLifetimeBolt(lifetime_threshold),5)
                .setNumTasks(10)
                .shuffleGrouping(FILTER_BOLT);

        /* Data grouped by ID field and computed intermediate ranking by lifetime value */
        builder.setBolt(PARTIAL_RANK_BOLT, new PartialRankBolt(rank_size), 3)
                .setNumTasks(6)
                .fieldsGrouping(FILTER_BY_LIFETIME_BOLT, new Fields(Constants.ID));

        /* Global ranking f the first K lamps with greater "lifetime" */
        builder.setBolt(GLOBAL_RANK_BOLT, new GlobalRankBolt(rank_size),3)
                .setNumTasks(6)
                .allGrouping(PARTIAL_RANK_BOLT);


        /* LOCAL MODE */
        /*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(QUERY_2_TOPOLOGY, config, builder.createTopology());

        Thread.sleep(600000);

        cluster.killTopology(QUERY_2_TOPOLOGY);
        cluster.shutdown(); */

        StormSubmitter.submitTopology(QUERY_2_TOPOLOGY,config,builder.createTopology());

    }
}
