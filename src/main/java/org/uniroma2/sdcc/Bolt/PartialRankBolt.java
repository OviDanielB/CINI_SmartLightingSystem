package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.Ranking.OldestKRanking;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * This Bolt classifies arriving tuple from FilteringByLifetimeBolt
 * based by the value of increasing lifetime (older lamp, higher rank position)
 * restricted to a partial group of global tuple.
 **/

public class PartialRankBolt extends BaseRichBolt {

    public static String RANKING = "ranking";

    private OutputCollector collector;
    private OldestKRanking ranking;
    private Gson gson;
    private int k;



    public PartialRankBolt(int k) {
        this.k = k;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.ranking = new OldestKRanking(k);
        this.gson = new Gson();
    }

    /**
     * Updating partial ranking of the first K oldest lamps when a new tuple
     * from from FilteringByLifetimeBolt is received
     *
     * @param tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        int id = (int) tuple.getValueByField(Constant.ID);
        Address address = (Address) tuple.getValueByField(Constant.ADDRESS);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(Constant.LIFETIME);
        Long timestamp = (Long) tuple.getValueByField(Constant.TIMESTAMP);

        /* Update local rank */
        RankLamp rankLamp = new RankLamp(id, address, lifetime, timestamp);
        boolean updated = ranking.update(rankLamp);

		/* Emit if the local oldest K is changed */
        if (updated) {
            List<RankLamp> oldestK = ranking.getOldestK();

            String serializedRanking = gson.toJson(oldestK);

            Values values = new Values();
            values.add(serializedRanking);

            collector.emit(values);
        }

        collector.ack(tuple);
    }


    /**
     * Define which fields are sent to GlobalRankBolt:
     * RANKING: partial ranking
     *
     * @param outputFieldsDeclarer fields sent
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(PartialRankBolt.RANKING));
    }

}
