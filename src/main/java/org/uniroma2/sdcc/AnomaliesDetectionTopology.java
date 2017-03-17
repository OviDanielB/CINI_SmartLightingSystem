package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

public class AnomaliesDetectionTopology {

    public AnomaliesDetectionTopology() {
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("rabbitSpout", new RabbitMQSpout());
        builder.setBolt("filterBolt", new FilteringBolt()).shuffleGrouping("rabbitSpout");


        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("req1", config, builder.createTopology());

        Thread.sleep(60000);

        cluster.killTopology("req1");
        cluster.shutdown();

    }

}
