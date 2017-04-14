package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AddressNumberType;
import org.uniroma2.sdcc.Utils.Ranking.OldestKRanking;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * This Bolt merges rankings arriving from PartialRankBolts
 * obtaining a unique global ranking of the first K oldest lamps
 * and the number of lamps older than LIFETIME_THRESHOLD.
 **/

public class GlobalRankBolt extends BaseRichBolt implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LOG_TAG = "[CINI] [GlobalRankBolt] ";
    private OutputCollector collector;
    private OldestKRanking ranking;
    private int K;
    private Gson gson;
    private Type listType;

    /* rabbitMQ connection */
    private final static String RABBIT_HOST = "localhost";
    private final static Integer RABBIT_PORT = 5673;
    private  final String  EXCHANGE_NAME = "dashboard_exchange";
    /* topic based pub/sub */
    private  final String EXCHANGE_TYPE = "topic";
    private  final String ROUTING_KEY = "dashboard.rank";
    private Connection connection;
    private Channel channel;


    private final static String MEMCAC_HOST = "localhost";
    private final static Integer MEMCAC_PORT = 11211;
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
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCAC_HOST, MEMCAC_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }

        establishRabbitConnection();
    }

    /**
     * connect to RabbitMQ to send ranking info to
     * dashboard
     */
    private void establishRabbitConnection() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT_HOST);
        factory.setPort(RABBIT_PORT);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME,EXCHANGE_TYPE);
            System.out.println(LOG_TAG + "Rabbit connection established on " + RABBIT_HOST + "/" + RABBIT_PORT);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            System.out.println(LOG_TAG + "Rabbit Connection Failed.");
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

        String json_ranking = memcachedClient.get("current_global_rank").toString();
        HashMap<Integer,Integer> oldIds = (HashMap<Integer, Integer>)
                memcachedClient.get("old_counter");

        // send to dashboard only if ranking has been updated
        if (rankingUpdated(json_ranking)) {

            String json_results;

            try {
                /* send to queue with routing key */
                if (json_ranking != null) {

                    RankingResults results = new RankingResults(
                            gson.fromJson(json_ranking,listType), oldIds.size());

                    json_results = gson.toJson(results);

                    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, json_results.getBytes());

                    System.out.println(LOG_TAG + "Sent : " + json_results);
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(LOG_TAG + "Failed sending mess to Rabbit.");
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

        String json_sent_ranking;
        List<RankLamp> sent_ranking;
        try{
            json_sent_ranking = memcachedClient.get("sent_global_ranking").toString();
            sent_ranking = gson.fromJson(json_sent_ranking, listType);

        } catch (Exception e) {
            sent_ranking = new ArrayList<>();
        }
        List<RankLamp> current_ranking = gson.fromJson(json_ranking, listType);
        for (int i=0; i<current_ranking.size(); i++) {
            if (sent_ranking.size()==0 || current_ranking.get(i).getId() != sent_ranking.get(i).getId()
                    ) {
                // updated last valid global ranking in memory
                // if ranking list contains different lamp IDs or in a different order
                // or no previous valid global ranking was sent
                memcachedClient.set("sent_global_ranking",0, json_ranking);
                return true;
            }
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

            memcachedClient.set("current_global_rank",0, json_ranking);
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
     * retain only valuable information for address
     * @param address type
     * @return ex: Via Politecnico 23 (if number is a civic number)
     *              / Via Politecnico km 2000 (if number is a km number)
     */
    private String composeAddressString(Address address) {

        String finalAddress;
        if (address.getNumberType()== AddressNumberType.CIVIC) {
            finalAddress = String.format("%s %s",address.getName(), address.getNumber());
        } else {
            finalAddress = String.format("%s km %s",address.getName(), address.getNumber());
        }
        return finalAddress;
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
