package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.*;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;

/**
 * Topology produces statistics on energy consumption for street-lamps in a configurable window
 * of time through the windowLengthInSeconds argument. Statistics are emitted every tickFrequencyInSeconds
 * seconds.
 * Example 1:
 * If windowLengthInSeconds = 3600 seconds and tickFrequencyInSeconds = 60 then
 * the topology emits every minute the average value on the consumption calculated in the last hour.
 * <p>
 * If windowLengthInSeconds = 3600 and tickFrequencyInSeconds = 3600 then the topology emits
 * slotted hour statistics.
 *
 * @author Emanuele
 */
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

        builder.setBolt("filterBolt", new FilteringBolt(), 1).shuffleGrouping("rabbitSpout");

        builder.setBolt("parser", new ParserBolt(), 1).shuffleGrouping("filterBolt");

        builder.setBolt("HourlyBolt", new IndividualConsumptionBolt(3600,
                tickFrequencyInSeconds) {
        }, 1).shuffleGrouping("parser");

        builder.setBolt("AggregateHourly", new AggregateConsumptionBolt(3600,
                tickFrequencyInSeconds) {
        }, 3)
                .fieldsGrouping("parser", new Fields("street"));

        builder.setBolt("DailyBolt", new DailyIndividualConsumptionBolt(3600 * 24,
                3600 / 2, tickFrequencyInSeconds), 1)
                .shuffleGrouping("HourlyBolt");

        builder.setBolt("AggregateDaily", new DailyAggregateConsumptionBolt(3600 * 24,
                3600 / 2, tickFrequencyInSeconds), 3)
                .fieldsGrouping("AggregateHourly", new Fields("street"));

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("req2", config, builder.createTopology());

        Thread.sleep(3600 * 1000);

        cluster.killTopology("req2");
        cluster.shutdown();

//        StormSubmitter.submitTopology("req2", config, builder.createTopology());

    }

}
