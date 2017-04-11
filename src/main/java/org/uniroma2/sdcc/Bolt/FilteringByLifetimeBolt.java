package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.mockito.cglib.core.Local;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This Bolt processes arriving tuple from FilteringBolt
 * to reject which ones describe lamps with a value of lifetime
 * field smaller than a determinate LIFETIME_THRESHOLD
 */

public class FilteringByLifetimeBolt extends BaseRichBolt {

    private OutputCollector collector;
    /*  days threshold to be considered for ranking  TODO da mettere come parametro di configurazione */
    private int LIFETIME_THRESHOLD = 7;

    public FilteringByLifetimeBolt() {
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    /**
     * Parse values of: ID, address, lifetime, timestamp from tuple
     * received from FilteringBolt and sent the not rejected tuple to
     * the PartialRankBolt
     *
     * @param tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        int id =                (int) tuple.getValueByField(Constant.ID);
//        boolean state = (boolean) tuple.getValueByField(Constant.ON);
        String address =        tuple.getValueByField(Constant.ADDRESS).toString();
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(Constant.LIFETIME);
        Long timestamp =   (Long) tuple.getValueByField(Constant.TIMESTAMP);

        emitClassifiableLampTuple(tuple, id, address, lifetime, timestamp);
        collector.ack(tuple);

    }

    /**
     * Check and emit only tuple with value of lifetime > LIFETIME_THRESHOLD
     *
     * @param tuple received from FilteringBolt
     * @param id parsed from tuple
     * @param address parsed from tuple
     * @param lifetime parsed from tuple
     * @param timestamp parsed from tuple
     */
    private void emitClassifiableLampTuple(
            Tuple tuple, int id, String address, LocalDateTime lifetime, Long timestamp) {

        if (isOlderThan(lifetime)) {
            collector.emit(tuple, new Values(id, address, lifetime, timestamp));
        }
    }

    /**
     * Calculating if days from the last lamp replacement
     * indicating in the field "lifetime" are more than
     * LIFETIME_THRESHOLD to be considered quite old.
     *
     * @param lifetime value of lamp to be considered
     *
     * @return true if quite old, false otherwise
     */
    private boolean isOlderThan(LocalDateTime lifetime) {

        LocalDateTime d2 = LocalDateTime.now();
        /* difference between now and lifetime */
        long diff = ChronoUnit.DAYS.between(lifetime,d2);

        return diff > LIFETIME_THRESHOLD;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(Constant.ID, Constant.ADDRESS,
                Constant.LIFETIME, Constant.TIMESTAMP));
    }
}
