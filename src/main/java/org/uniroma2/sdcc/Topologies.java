package org.uniroma2.sdcc;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.uniroma2.sdcc.Bolt.*;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.*;
import org.uniroma2.sdcc.ControlSystem.CentralController.AnalyzeBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.ExecuteBolt;
import org.uniroma2.sdcc.ControlSystem.CentralController.PlanBolt;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;
import org.uniroma2.sdcc.Utils.Config.RankingConfig;
import org.uniroma2.sdcc.Utils.Config.StatisticsBoltConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;

/**
 * @author emanuele
 */
public class Topologies {

    private final static String RABBIT_SPOUT = "rabbitSpout";
    private final static String FILTER_BOLT = "filterBolt";
    private final static String FILTER_BY_LIFETIME_BOLT = "filterByLifetimeBolt";
    private final static String PARTIAL_RANK_BOLT = "partRankBolt";
    private final static String GLOBAL_RANK_BOLT = "globalRankBolt";
    private final static int RANK_SIZE_DEFAULT = 10;

    private final static String MALFUNCTION_CHECK_BOLT = "malfCheckBolt";
    private final static String NOT_RESPONDING_LAMP_BOLT = "weatherBolt";
    private final static String ANALYZE_CONTROL_BOLT = "analyzeBolt";
    private final static String PLAN_CONTROL_BOLT = "planBolt";
    private final static String EXECUTE_CONTROL_BOLT = "executeBolt";

    private final static int TICKTIME_DEFAULT = 60;
    private final static int HOURLY_WINDOWSLEN = 3600;
    private final static int DAILY_WINDOWLEN = 3600 * 24;
    private final static int DAILY_EMIT_FREQUENCY = 1800;
    private final static int WEEKLY_WINDOWLEN = DAILY_WINDOWLEN * 7;
    private final static int WEEKLY_EMIT_FREQUENCY = 3600 * 24;

    public static void main(String[] args) throws Exception {

        int rank_size = RANK_SIZE_DEFAULT;
        int tickfrequency = TICKTIME_DEFAULT;
        int hourly_window = HOURLY_WINDOWSLEN;
        int daily_window = DAILY_WINDOWLEN;
        int daily_emit_frequency = DAILY_EMIT_FREQUENCY;

        Config config = new Config();
        config.setNumWorkers(3);
        //config.setDebug(true);
        //config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {
            RankingConfig rankingConfig = yamlConfigRunner.getConfiguration()
                    .getRankingTopologyParams();
            rank_size = rankingConfig.getRank_size();

            StatisticsBoltConfig statisticsBoltConfig = yamlConfigRunner.getConfiguration()
                    .getStatisticsTopologyParams();
            tickfrequency = statisticsBoltConfig.getTickTupleFrequency();
            hourly_window = statisticsBoltConfig.getHourlyStatistics().get("windowLength");
            daily_window = statisticsBoltConfig.getDailyStatistics().get("windowLength");
            daily_emit_frequency = statisticsBoltConfig.getDailyStatistics().get("emitFrequency");

        } catch (IOException e) {
            e.printStackTrace();
        }

        TopologyBuilder builder = new TopologyBuilder();

        /*
            COMMON BOLTS
         */

        /* Lamps' data source  */
        builder.setSpout(RABBIT_SPOUT, new RabbitMQSpout(), 3);

        /* Check of format correctness of received tuples   */
        builder.setBolt(FILTER_BOLT, new FilteringBolt(), 3)
                .setNumTasks(6)
                .shuffleGrouping(RABBIT_SPOUT);


        /*
            RANKING TOPOLOGY BOLTS
         */

        /* Filtering from lamps which have been replace within LIFETIME_THRESHOLD days from now */
        builder.setBolt(FILTER_BY_LIFETIME_BOLT, new FilteringByLifetimeBolt())
                .allGrouping(FILTER_BOLT);

        /* Data grouped by "lifetime" field and partially sorted by it   */
        builder.setBolt(PARTIAL_RANK_BOLT, new PartialRankBolt(rank_size))
                .shuffleGrouping(FILTER_BY_LIFETIME_BOLT);

        /* Global ranking f the first K lamps with greater "lifetime" */
        builder.setBolt(GLOBAL_RANK_BOLT, new GlobalRankBolt(rank_size))
                .allGrouping(PARTIAL_RANK_BOLT);

        /*
            CONSUMPTION BOLT BOLTS
         */
        builder.setBolt("parser", new ParserBolt()).allGrouping(FILTER_BOLT);

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

        builder.setBolt("printer", new PrinterBolt(), 1)
                .shuffleGrouping("HourlyBolt")
                .shuffleGrouping("AggregateHourly")
                .shuffleGrouping("AggregateDaily")
                .shuffleGrouping("DailyBolt")
                .shuffleGrouping("AggregateWeekly")
                .shuffleGrouping("WeeklyBolt");

        /*
            ANOMALIES DETENCTION BOLTS
            *** CONTROL SYSTEM ***
         */

        builder.setBolt(MALFUNCTION_CHECK_BOLT, new MalfunctionCheckBolt())
                .allGrouping(FILTER_BOLT)
                .fieldsGrouping(FILTER_BOLT, new Fields(Constants.ADDRESS));

        builder.setBolt(NOT_RESPONDING_LAMP_BOLT, new NotRespondingLampBolt(), 3)
                .setNumTasks(5)
                .fieldsGrouping(MALFUNCTION_CHECK_BOLT, new Fields(Constants.ID));

        builder.setBolt(ANALYZE_CONTROL_BOLT, new AnalyzeBolt(), 5)
                .setNumTasks(10)
                .fieldsGrouping(NOT_RESPONDING_LAMP_BOLT, new Fields(Constants.ADDRESS));

        builder.setBolt(PLAN_CONTROL_BOLT, new PlanBolt(), 3)
                .setNumTasks(6)
                .fieldsGrouping(ANALYZE_CONTROL_BOLT, new Fields(Constants.ID));

        builder.setBolt(EXECUTE_CONTROL_BOLT, new ExecuteBolt(), 10)
                .setNumTasks(20)
                .fieldsGrouping(PLAN_CONTROL_BOLT, new Fields(Constants.ID));


        /*
            DECLARE CLUSTER AND SUBMIT TOPOLOGY
         */

        /*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Monitoring", config, builder.createTopology());

        Thread.sleep(3600 * 1000);

        cluster.killTopology("Monitoring");
        cluster.shutdown(); */

        StormSubmitter.submitTopology("Monitoring", config, builder.createTopology());
    }

}
