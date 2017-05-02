package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.JSONConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * This Bolt processes arriving tuple from FilteringBolt
 * to reject which ones describe lamps with a value of lifetime
 * field smaller than a determinate LIFETIME_THRESHOLD
 */

public class FilteringByLifetimeBolt extends BaseRichBolt {

    private OutputCollector collector;
    /*  days threshold to be considered for ranking */
    private int lifetime_threshold;
    /*  number of old lamps  */
    private HashMap<Integer, Integer> oldIds;
    private CacheManager cache;

    public FilteringByLifetimeBolt(int lifetime_threshold) {
        this.lifetime_threshold = lifetime_threshold;
    }

    /**
     * Bolt initialization
     *
     * @param map             map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.oldIds = new HashMap<>();
        this.cache = new MemcachedManager();
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

        try {
            oldIds = cache.getIntIntMap(MemcachedManager.OLD_COUNTER);
        } catch (Exception e) {
            this.oldIds = new HashMap<>();
        }

        int id = (int) tuple.getValueByField(Constants.ID);
        Address address = (Address) tuple.getValueByField(Constants.ADDRESS);
        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(Constants.LIFETIME);
        Long timestamp = (Long) tuple.getValueByField(Constants.TIMESTAMP);

        emitClassifiableLampTuple(tuple, id, address, lifetime, timestamp);

        collector.ack(tuple);
    }

    /**
     * Check and emit only tuple with value of lifetime > LIFETIME_THRESHOLD
     * and save id in memory if true.
     *
     * @param tuple     received from FilteringBolt
     * @param id        parsed from tuple
     * @param address   parsed from tuple
     * @param lifetime  parsed from tuple
     * @param timestamp parsed from tuple
     */
    private void emitClassifiableLampTuple(
            Tuple tuple, int id, Address address, LocalDateTime lifetime, Long timestamp) {

        if (isOlderThan(lifetime)) {
            oldIds.put(id, 1);
            cache.put(MemcachedManager.OLD_COUNTER, JSONConverter.fromHashMapIntInt(oldIds));

            collector.emit(tuple, new Values(id, address, lifetime, timestamp));
        } else {
            if (oldIds.containsKey(id)) {
                oldIds.remove(id);
            }
            cache.put(MemcachedManager.OLD_COUNTER, JSONConverter.fromHashMapIntInt(oldIds));
        }
    }

    /**
     * Calculating if days from the last lamp replacement
     * indicating in the field "lifetime" are more than
     * LIFETIME_THRESHOLD to be considered quite old.
     *
     * @param lifetime value of lamp to be considered
     * @return true if quite old, false otherwise
     */
    private boolean isOlderThan(LocalDateTime lifetime) {

        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime d2 = currentDate.toLocalDateTime();
        /* difference between now and lifetime */
        long diff = ChronoUnit.DAYS.between(lifetime, d2);

        return diff > lifetime_threshold;
    }

    /**
     * Declare name of the output tuple fields.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.ID,
                Constants.ADDRESS,
                Constants.LIFETIME,
                Constants.TIMESTAMP));
    }
}
