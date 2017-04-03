package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.DailyConsumptionBolt;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.HourlyConsumptionBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

public class ConsumptionStatisticsTopology {

    public ConsumptionStatisticsTopology() {
    }

    public static void main(String[] args) throws Exception {
        Config config = new Config();

        int tickFrequencyInSeconds = 60;
        config.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequencyInSeconds);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("rabbitSpout", new RabbitMQSpout());

        builder.setBolt("filterBolt", new FilteringBolt(), 3).shuffleGrouping("rabbitSpout");

        builder.setBolt("hourStatBolt", new HourlyConsumptionBolt(3600, tickFrequencyInSeconds),
                3).fieldsGrouping("filterBolt", new Fields(Constant.ADDRESS));

        builder.setBolt("dailyStatBolt", new DailyConsumptionBolt(3600 * 24,
                3600, 5 * 60, tickFrequencyInSeconds), 3)
                .fieldsGrouping("filterBolt", new Fields(Constant.ADDRESS));

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("req2", config, builder.createTopology());

        Thread.sleep(3600 * 1000);

        cluster.killTopology("req2");
        cluster.shutdown();
    }

}
