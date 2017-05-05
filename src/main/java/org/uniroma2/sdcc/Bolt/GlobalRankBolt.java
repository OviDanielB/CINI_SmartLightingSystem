package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedPeriodicUpdater;
import org.uniroma2.sdcc.Utils.Cache.MemcachedWriterRunnable;
import org.uniroma2.sdcc.Utils.JSONConverter;
import org.uniroma2.sdcc.Utils.MOM.PubSubManager;
import org.uniroma2.sdcc.Utils.MOM.RabbitPubSubManager;
import org.uniroma2.sdcc.Utils.Ranking.OldestKRanking;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Bolt merges rankings arriving from PartialRankBolts
 * obtaining a unique global ranking of the first K oldest lamps
 * and the number of lamps older than LIFETIME_THRESHOLD.
 **/

public class GlobalRankBolt extends BaseRichBolt implements Serializable {

    private static final long serialVersionUID = 1L;
    private OutputCollector collector;
    private OldestKRanking ranking;
    private int K;

    private ConcurrentHashMap<String, String> keyValueMem;

    /* accessed by multiple threads */
    private volatile String json_ranking;
    private volatile HashMap<Integer,Integer> oldIds;
    private volatile String json_sent_ranking;

    /* topic based pub/sub on rabbitMQ */
    private PubSubManager pubSubManager;
    private final String ROUTING_KEY = "dashboard.rank";

    protected CacheManager cache;
    /* frequency to send new ranking (if updated compared to the previous one) */
    private int UPDATE_FREQUENCY = 60;

    private MemcachedWriterRunnable writer;


    public GlobalRankBolt(int K) {
        this.K = K;
    }

    /**
     * Bolt initialization
     *
     * @param map map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;

        initializeVars();

        startPeriodicUpdate();

        this.cache = new MemcachedManager();
        /* connect to rabbit with attributes taken from config file */
        pubSubManager = new RabbitPubSubManager();
    }

    /**
     * Start periodic update of values from cache
     * using thread to reduce response time for every tuple
     */
    private void startPeriodicUpdate() {
        Timer timer = new Timer();
        timer.schedule(new PeriodicUpdater(),0,UPDATE_FREQUENCY / 2 * 1000);

        writer = new MemcachedWriterRunnable(keyValueMem);
        writer.start();
    }

    /**
     * Initialize useful variables.
     */
    private void initializeVars() {

        this.ranking = new OldestKRanking(K);
        this.keyValueMem = new ConcurrentHashMap<>();
        json_ranking = "{}";
        json_sent_ranking = "{}";
        oldIds = new HashMap<>();
    }

    /**
     * Updating global ranking of the first K oldest lamps when a new tuple
     * from from PartialRankBolt is received, sending the result periodically,
     * when a Tick Tuple is received.
     *
     * @param tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        if (TupleHelpers.isTickTuple(tuple))
            sendWindowRank(); // send global ranking every tick tuple
        else
            getRanking(tuple); // update global ranking

        collector.ack(tuple);
    }

    /**
     * Save in memory the last value of the resulting global ranking.
     * and publish it to the topic related to the dashboard on output queue.
     */
    private void sendWindowRank() {
        // publish on output queue only if ranking has been updated
        if (rankingUpdated(json_ranking)) {

            String json_results;

            /* send to queue with routing key */
            RankingResults results = new RankingResults(
                        JSONConverter.toRankLampListData(json_ranking), oldIds.size());

            json_results = JSONConverter.fromRankingResults(results);

            /* publish on queue */
            pubSubManager.publish(ROUTING_KEY, json_results);
        }
    }


    /**
     * Compare list of last ranking sent (sent_ranking) saved in memory
     * with that one of last ranking calculated (current_ranking).
     *
     * @param json_ranking current ranking saved
     * @return true if sent_ranking has been updated as current_ranking values
     */
    protected boolean rankingUpdated(String json_ranking) {
        /* convert to json */
        List<RankLamp> sent_ranking = JSONConverter.toRankLampListData(json_sent_ranking);
        List<RankLamp> current_ranking = JSONConverter.toRankLampListData(json_ranking);

        /* if two lists are different, update cache */
        if( !sameElements(current_ranking,sent_ranking)){
            keyValueMem.put(MemcachedManager.SENT_GLOBAL_RANKING,json_ranking);
            return true;
        }
        return false;
    }

    /**
     * Checks if list have identical elements
     *
     * @param current_ranking list of last computed ranking
     * @param sent_ranking list of last sent ranking
     * @return true if are the same, false otherwise
     */
    private boolean sameElements(List<RankLamp> current_ranking, List<RankLamp> sent_ranking) {

        if (current_ranking.size() != sent_ranking.size()) {
            return false;
        }

        if (current_ranking.size() == 0) {
            return false;
        }

        for (int i = 0; i < current_ranking.size(); i++) {
            if(current_ranking.get(i) != sent_ranking.get(i)){
                return false;
            }
        }
        return true;
    }

    /**
     * Get values of the resulting partial ranking from PartialRankBolt and
     * merge them in a global ranking.
     *
     * @param tuple received
     */
    private void getRanking(Tuple tuple) {

        String serializedRanking = tuple.getStringByField(org.uniroma2.sdcc.Constants.RANKING);

        List<RankLamp> partialRanking = JSONConverter.toRankLampListData(serializedRanking);

		/* Update global rank */
        boolean updated = false;
        for (RankLamp lamp : partialRanking){
            updated |= ranking.update(lamp);
        }

		/* Save in memory if the local ranking K is changed */
        if (updated) {

            List<RankLamp> globalOldestK = ranking.getOldestK();

            String json_ranking = JSONConverter.fromRankLampList(globalOldestK);

            keyValueMem.put(MemcachedManager.CURRENT_GLOBAL_RANK,json_ranking);
        }
    }

    /**
     * Configure a frequency of Tick Tuple every 60 sec
     * to determine how often emit a new ranking of lamps (if .
     *
     * @return conf
     */
    @Override
    public Map<String, Object> getComponentConfiguration() {
        // configure how often a tick tuple will be sent to our bolt
        return TupleHelpers.getTickTupleFrequencyConfig(UPDATE_FREQUENCY);
    }

    /**
     * Declare name of the output tuple fields.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // nothing to declare
    }

    /**
     * Periodically reads from memory values of:
     * - current global ranking, lst computed ranking list
     * - sent global ranking, last sent ranking list
     * - list of all IDs related to all lamps noticed as older
     *   than LIFETIME_THRESHOLD
     */
    private class PeriodicUpdater extends MemcachedPeriodicUpdater{

        @Override
        public void run() {
            json_ranking = this.cache.getString(MemcachedManager.CURRENT_GLOBAL_RANK);
            oldIds = this.cache.getIntIntMap(MemcachedManager.OLD_COUNTER);
            json_sent_ranking = this.cache.getString(MemcachedManager.SENT_GLOBAL_RANKING);
        }
    }
}
