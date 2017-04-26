package org.uniroma2.sdcc.Bolt;

import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.HeliosLog;
import org.uniroma2.sdcc.Utils.JSONConverter;
import org.uniroma2.sdcc.Utils.MOM.PubSubManager;
import org.uniroma2.sdcc.Utils.MOM.RabbitPubSubManager;
import org.uniroma2.sdcc.Utils.Ranking.OldestKRanking;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Bolt merges rankings arriving from PartialRankBolts
 * obtaining a unique global ranking of the first K oldest lamps
 * and the number of lamps older than LIFETIME_THRESHOLD.
 **/

public class GlobalRankBolt extends BaseRichBolt implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LOG_TAG = "[GlobalRankBolt]";
    private OutputCollector collector;
    private OldestKRanking ranking;
    private int K;


    /* topic based pub/sub on rabbitMQ */
    private PubSubManager pubSubManager;
    private final String ROUTING_KEY = "dashboard.rank";

    /* cache connection attributes */
    private final static String MEMCAC_HOST = "localhost";
    private final static Integer MEMCAC_PORT = 11211;
    private CacheManager cache;



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
        this.ranking = new OldestKRanking(K);

        this.cache = new MemcachedManager(MEMCAC_HOST,MEMCAC_PORT);

        /* connect to rabbit with attributes taken from config file */
        pubSubManager = new RabbitPubSubManager();

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

        if (isTickTuple(tuple)) {
            /*  send global ranking every tick tuple    */
            sendWindowRank();
        } else {
            /*  update global ranking    */
            getRanking(tuple);
        }
        collector.ack(tuple);
    }

    /**
     * Save in memory the last value of the resulting global ranking.
     * [...] and sent it to the dashboard [...]
     */
    private void sendWindowRank() {

        /* get values from cache */
        String json_ranking = cache.getString(MemcachedManager.CURRENT_GLOBAL_RANK);
        HashMap<Integer,Integer> oldIds = cache.getIntIntMap(MemcachedManager.OLD_COUNTER);

        // send to dashboard only if ranking has been updated
        if (rankingUpdated(json_ranking)) {

            String json_results;

            /* send to queue with routing key */
            if (json_ranking != null) {

                RankingResults results = new RankingResults(
                        JSONConverter.toRankLampListData(json_ranking), oldIds.size());

                json_results = JSONConverter.fromRankingResults(results);

                /* publish on queue */
                pubSubManager.publish(ROUTING_KEY, json_results);

                HeliosLog.logOK(LOG_TAG, "Sent : " + json_results);

            }

        }
    }

    /**
     * Compare list of last ranking sent (sent_ranking) saved in memory
     * with that one of last ranking calculated (current_ranking).
     *
     * @param json_ranking current ranking saved
     * @return true if sent_ranking has been updated as current_ranking values
     */
    private boolean rankingUpdated(String json_ranking) {

        String json_sent_ranking = cache.getString(MemcachedManager.SENT_GLOBAL_RANKING);

        /* convert to json */
        List<RankLamp> sent_ranking = JSONConverter.toRankLampListData(json_sent_ranking);
        List<RankLamp> current_ranking = JSONConverter.toRankLampListData(json_ranking);

        /* if two lists are different, update cache */
        if(current_ranking.stream().filter(e -> {
            Integer index =  current_ranking.indexOf(e);
            return  e.getId() != sent_ranking.get(index).getId();
        }).count() > 0) {

            cache.put(MemcachedManager.SENT_GLOBAL_RANKING, json_ranking);
            return true;
        }

        return false;
    }

    /**
     * Get values of the resulting partial ranking from PartialRankBolt and
     * merge them in a global ranking.
     *
     * @param tuple received
     */
    private void getRanking(Tuple tuple) {

        String serializedRanking = tuple.getStringByField(PartialRankBolt.RANKING);

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

            cache.put(MemcachedManager.CURRENT_GLOBAL_RANK, json_ranking);
        }
    }

    /**
     * Configure a frequency of Tick Tuple every 60 sec
     * to determine how often emit a new ranking of lamps.
     *
     * @return conf
     */
    @Override
    public Map<String, Object> getComponentConfiguration() {
        // configure how often a tick tuple will be sent to our bolt
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 60);
        return conf;
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

    /** Check if the received tuple is a Tick Tuple
     *
     * @param tuple tuple to check
     **/
    private static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }
}
