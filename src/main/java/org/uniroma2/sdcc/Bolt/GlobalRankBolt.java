package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.OldestKRanking;
import org.uniroma2.sdcc.Utils.RankLamp;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private Gson gson;
    private Type listType;


    private MemcachedClient memcachedClient;


    public GlobalRankBolt(int K) {
        this.K = K;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.ranking = new OldestKRanking(K);
        this.gson = new Gson();
        this.listType = new TypeToken<ArrayList<RankLamp>>(){}.getType();

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress("localhost", 11211));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // TODO sent to dashboard
                    /* INIT BLOCK TO DESERIALIZE */
        String get_json_ranking = (String) memcachedClient.get("global_rank");

        List<RankLamp> rank_from_memory = gson.fromJson(get_json_ranking, listType);

        System.out.println(
                "GLOBAL Oldest " +
                        K +
                        " (of " +
                        memcachedClient.get("old_counter") +
                        " lamps: (SIZE " +
                        rank_from_memory.size() +
                        ")\n");
        for (RankLamp aGlobalOldestK : rank_from_memory) {
            System.out.println("ID: " + aGlobalOldestK.getId() +
                    " Address: " + aGlobalOldestK.getAddress() +
                    " Last-Substitution: " + aGlobalOldestK.getLifetime() + "\n");
        }
            /* END BLOCK TO DESERIALIZE */
    }

    /**
     * Get values of the resulting partial ranking from PartialRankBolt and
     * merge them in a global ranking.
     *
     * @param tuple received
     */
    private void getRanking(Tuple tuple) {

        String serializedRanking = tuple.getStringByField(PartialRankBolt.RANKING);

        List<RankLamp> partialRanking = gson.fromJson(serializedRanking, listType);

		/* Update global rank */
        boolean updated = false;
        for (RankLamp lamp : partialRanking){
            updated |= ranking.update(lamp);
        }

		/* Save in memory if the local ranking K is changed */
        if (updated) {

            List<RankLamp> globalOldestK = ranking.getOldestK();

            String json_ranking = gson.toJson(globalOldestK);

            memcachedClient.set("global_rank",3600, json_ranking);

//             Shutdowns the memcached client
//            memcachedClient.shutdown();

//            System.out.println("GLOBAL Oldest 10 lamps: (SIZE "+globalOldestK.size() +")\n");
//            for (RankLamp aGlobalOldestK : globalOldestK) {
//                System.out.println("ID: " + aGlobalOldestK.getId() +
//                        " Address: " + aGlobalOldestK.getAddress().toString() +
//                        " Last-Substitution: " + aGlobalOldestK.getLifetime() + "\n");
//            }
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

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
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