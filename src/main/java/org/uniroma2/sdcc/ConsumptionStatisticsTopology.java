package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.*;
import org.uniroma2.sdcc.Bolt.FilteringBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;
import org.uniroma2.sdcc.Utils.Config.StatisticsBoltConfig;
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

    private final static String SPOUT_BOLT = "rabbitSpout";
    private final static String FILTERING_BOLT = "filterBolt";
    private final static String PARSER_BOLT = "parser";
    private final static String INDIVIDUAL_HOURLY_BOLT = "HourlyBolt";
    private final static String AGGREGATE_HOURLY_BOLT = "AggregateHourly";
    private final static String INDIVIDUAL_DAILY_BOLT = "DailyBolt";
    private final static String AGGREGATE_DAILY_BOLT = "AggregateDaily";
    private final static String INDIVIDUAL_WEEKLY_BOLT = "WeeklyBolt";
    private final static String AGGREGATE_WEEKLY_BOLT = "AggregateWeekly";
    private final static String PRINTER_BOLT = "printer";

    private final static String GROUP_FIELD = "street";

    public ConsumptionStatisticsTopology() {
    }

    public static void main(String[] args) throws Exception {

        int tickfrequency;
        int hourly_window;
        int daily_window;
        int daily_emit_frequency;

        Config config = new Config();
        config.setNumWorkers(4);
        config.setMessageTimeoutSecs(5);

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {
            StatisticsBoltConfig statisticsBoltConfig = yamlConfigRunner.getConfiguration()
                    .getStatisticsTopologyParams();

            tickfrequency = statisticsBoltConfig.getTickTupleFrequency();
            hourly_window = statisticsBoltConfig.getHourlyStatistics().get("windowLength");
            daily_window = statisticsBoltConfig.getDailyStatistics().get("windowLength");
            daily_emit_frequency = statisticsBoltConfig.getDailyStatistics().get("emitFrequency");

        } catch (IOException e) {
            tickfrequency = TICKTIME_DEFAULT;
            hourly_window = HOURLY_WINDOWSLEN;
            daily_window = DAILY_WINDOWLEN;
            daily_emit_frequency = DAILY_EMIT_FREQUENCY;
        }

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(SPOUT_BOLT, new RabbitMQSpout(), 2);

        builder.setBolt(FILTERING_BOLT, new FilteringBolt(), 2)
                .setNumTasks(5)
                .shuffleGrouping(SPOUT_BOLT);

        builder.setBolt(PARSER_BOLT, new ParserBolt(), 2)
                .setNumTasks(5)
                .shuffleGrouping(FILTERING_BOLT);

        builder.setBolt(INDIVIDUAL_HOURLY_BOLT, new IndividualConsumptionBolt(daily_window,
                tickfrequency), 2)
                .setNumTasks(5)
                .shuffleGrouping(PARSER_BOLT);

        builder.setBolt(AGGREGATE_HOURLY_BOLT, new AggregateConsumptionBolt(hourly_window, tickfrequency),
                3)
                .setNumTasks(6)
                .fieldsGrouping(PARSER_BOLT, new Fields(GROUP_FIELD));

        builder.setBolt(INDIVIDUAL_DAILY_BOLT, new ExtendendIndividualConsumptionBolt(daily_window,
                daily_emit_frequency, tickfrequency))
                .shuffleGrouping(INDIVIDUAL_HOURLY_BOLT);

        builder.setBolt(AGGREGATE_DAILY_BOLT, new ExtendedAggregateConsumptionBolt(daily_window,
                daily_emit_frequency, tickfrequency))
                .fieldsGrouping(AGGREGATE_HOURLY_BOLT, new Fields(GROUP_FIELD));

        builder.setBolt(INDIVIDUAL_WEEKLY_BOLT, new ExtendendIndividualConsumptionBolt(WEEKLY_WINDOWLEN,
                WEEKLY_EMIT_FREQUENCY, tickfrequency))
                .shuffleGrouping(INDIVIDUAL_DAILY_BOLT);

        builder.setBolt(AGGREGATE_WEEKLY_BOLT, new ExtendedAggregateConsumptionBolt(WEEKLY_WINDOWLEN,
                WEEKLY_EMIT_FREQUENCY, tickfrequency))
                .fieldsGrouping(AGGREGATE_DAILY_BOLT, new Fields(GROUP_FIELD));

        builder.setBolt(PRINTER_BOLT, new PrinterBolt(), 3)
                .setNumTasks(6)
                .shuffleGrouping(INDIVIDUAL_HOURLY_BOLT)
                .shuffleGrouping(AGGREGATE_HOURLY_BOLT)
                .shuffleGrouping(AGGREGATE_DAILY_BOLT)
                .shuffleGrouping(INDIVIDUAL_DAILY_BOLT)
                .shuffleGrouping(AGGREGATE_WEEKLY_BOLT)
                .shuffleGrouping(INDIVIDUAL_WEEKLY_BOLT);

        /*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("req2", config, builder.createTopology());

        Thread.sleep(3600 * 1000);

        cluster.killTopology("req2");
        cluster.shutdown(); */

        StormSubmitter.submitTopology("Query2", config, builder.createTopology());

    }

}
