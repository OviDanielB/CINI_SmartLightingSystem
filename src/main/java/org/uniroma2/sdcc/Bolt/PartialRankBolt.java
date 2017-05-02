package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.JSONConverter;
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

    private OutputCollector collector;
    protected OldestKRanking ranking;
    private int k;

    public PartialRankBolt(int k) {
        this.k = k;
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
        this.ranking = new OldestKRanking(k);
    }

    /**
     * Updating partial ranking of the first K oldest lamps when a new tuple
     * from from FilteringByLifetimeBolt is received
     *
     * @param tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        if (updateRanking(tuple))
            sendPartialRanking(); // Emit if the local oldest K is changed

        collector.ack(tuple);
    }

    /**
     * Update current partial ranking with new tuple arrived.
     *
     * @param tuple received
     * @return true if current ranking is updated
     */
    protected boolean updateRanking(Tuple tuple) {
        int id = (int) tuple.getValueByField(Constants.ID);
        Address address = (Address) tuple.getValueByField(Constants.ADDRESS);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(Constants.LIFETIME);
        Long timestamp = (Long) tuple.getValueByField(Constants.TIMESTAMP);

        /* Update local rank */
        RankLamp rankLamp = new RankLamp(id, address, lifetime, timestamp);
        return ranking.update(rankLamp);
    }

    /**
     * Send to GlobalRankBolt the new computed partial ranking.
     */
    private void sendPartialRanking() {

        List<RankLamp> oldestK = ranking.getOldestK();

        String serializedRanking = JSONConverter.fromRankLampList(oldestK);

        Values values = new Values();
        values.add(serializedRanking);

        collector.emit(values);
    }

    /**
     * Define which fields are sent to GlobalRankBolt:
     * RANKING: partial ranking
     *
     * @param outputFieldsDeclarer fields sent
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Constants.RANKING));
    }

}
