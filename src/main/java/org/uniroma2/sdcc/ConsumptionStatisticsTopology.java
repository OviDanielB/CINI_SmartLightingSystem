package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.*;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;
import org.uniroma2.sdcc.Utils.Config.ServiceConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;

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

    private final static int TICKTIME_DEFAULT = 60;
    private final static int HOURLY_WINDOWSLEN = 3600;
    private final static int DAILY_WINDOWLEN = 3600 * 24;
    private final static int DAILY_EMIT_FREQUENCY = 1800;
    private final static int WEEKLY_WINDOWLEN = DAILY_WINDOWLEN * 7;
    private final static int WEEKLY_EMIT_FREQUENCY = 3600 * 24;


    public ConsumptionStatisticsTopology() {
    }

    public static void main(String[] args) throws Exception {

        int tickfrequency;
        int hourly_window;
        int daily_window;
        int daily_emit_frequency;

        Config config = new Config();

        /*
        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner("/Users/ovidiudanielbarba/IdeaProjects/CINI_SmartLightingSystem/config/config.yml");

        try {
            ServiceConfig serviceConfig = yamlConfigRunner.getConfiguration()
                    .getStatisticsTopologyParams();

            tickfrequency = serviceConfig.getTickTupleFrequency();
            hourly_window = serviceConfig.getHourlyStatistics().get("windowLength");
            daily_window = serviceConfig.getDailyStatistics().get("windowLength");
            daily_emit_frequency = serviceConfig.getDailyStatistics().get("emitFrequency");

        } catch (IOException e) {
            tickfrequency = TICKTIME_DEFAULT;
            hourly_window = HOURLY_WINDOWSLEN;
            daily_window = DAILY_WINDOWLEN;
            daily_emit_frequency = DAILY_EMIT_FREQUENCY;
        } */

        tickfrequency = TICKTIME_DEFAULT;
        hourly_window = HOURLY_WINDOWSLEN;
        daily_window = DAILY_WINDOWLEN;
        daily_emit_frequency = DAILY_EMIT_FREQUENCY;

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("rabbitSpout", new RabbitMQSpout(), 2);

        builder.setBolt("filterBolt", new FilteringBolt(), 1).shuffleGrouping("rabbitSpout");

        builder.setBolt("parser", new ParserBolt(), 1).shuffleGrouping("filterBolt");

        builder.setBolt("HourlyBolt", new IndividualConsumptionBolt(daily_window,
                tickfrequency), 1).shuffleGrouping("parser");

        builder.setBolt("AggregateHourly", new AggregateConsumptionBolt(hourly_window, tickfrequency),
                3).fieldsGrouping("parser", new Fields("street"));

        builder.setBolt("DailyBolt", new ExtendendIndividualConsumptionBolt(daily_window,
                daily_emit_frequency, tickfrequency), 1)
                .shuffleGrouping("HourlyBolt");

        builder.setBolt("AggregateDaily", new ExtendedAggregateConsumptionBolt(daily_window,
                daily_emit_frequency, tickfrequency), 3)
                .fieldsGrouping("AggregateHourly", new Fields("street"));

        builder.setBolt("WeeklyBolt", new ExtendendIndividualConsumptionBolt(WEEKLY_WINDOWLEN,
                WEEKLY_EMIT_FREQUENCY, tickfrequency), 1)
                .shuffleGrouping("DailyBolt");

        builder.setBolt("AggregateWeekly", new ExtendedAggregateConsumptionBolt(WEEKLY_WINDOWLEN,
                WEEKLY_EMIT_FREQUENCY, tickfrequency), 3)
                .fieldsGrouping("AggregateDaily", new Fields("street"));

        builder.setBolt("printer", new PrinterBolt(args[1]), 1)
                .shuffleGrouping("HourlyBolt")
                .shuffleGrouping("AggregateHourly")
                .shuffleGrouping("AggregateDaily")
                .shuffleGrouping("DailyBolt")
                .shuffleGrouping("AggregateWeekly")
                .shuffleGrouping("WeeklyBolt");

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("req2", config, builder.createTopology());

        Thread.sleep(3600 * 1000);

        cluster.killTopology("req2");
        cluster.shutdown();

//        StormSubmitter.submitTopology("req2", config, builder.createTopology());

    }

}
