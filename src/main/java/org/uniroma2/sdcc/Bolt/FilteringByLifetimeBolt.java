package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedPeriodicUpdater;
import org.uniroma2.sdcc.Utils.JSONConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * This Bolt processes incoming tuples from FilteringBolt
 * to reject which ones describe lamps with a value of lifetime
 * field faraway minus than a determinate LIFETIME_THRESHOLD days
 * from the date of computation.
 */
public class FilteringByLifetimeBolt extends BaseRichBolt {

    private OutputCollector collector;
    /*  days threshold to be considered for ranking */
    private int lifetime_threshold;
    /*  number of old lamps  */
    private volatile HashMap<Integer, Integer> oldIds;

    private ExecutorService executorService;

    private ArrayBlockingQueue<String> queue;
    /* if queue capacity maximum => producer blocks on put operation,
      similarly capacity 0 => consumer blocks on take */
    private static final Integer QUEUE_CAPACITY = 1000;

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
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        startPeriodicUpdate();
        startThread();
    }

    /**
     * start periodic update of values from cache
     * using thread to increase response time
     */
    private void startPeriodicUpdate() {

        Timer timer = new Timer();
        timer.schedule(new PeriodicUpdater(), 0, 1000);
    }


    private void startThread() {
        executorService = Executors.newFixedThreadPool(2);
        IntStream.range(0, 2).forEach(e -> {
            executorService.submit(() -> {
                CacheManager manager = new MemcachedManager();
                String mess ;
                try {
                    while ((mess = queue.take()) != null) {
                        manager.put(MemcachedManager.OLD_COUNTER, mess);

                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
        });
    }


    /**
     * Check if the incoming tuple is referred to a lamp
     * that has to be involved in ranking computation.
     *
     * @param tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        emitClassifiableLampTuple(tuple);

        collector.ack(tuple);
    }

    /**
     * Check if tuple contains a value of lifetime > LIFETIME_THRESHOLD.
     * If true, emit tuple to PartialRankBolt, else reject it.
     *
     * @param tuple received from FilteringBolt
     */
    private void emitClassifiableLampTuple(Tuple tuple) {

        if (isOlderThan(tuple))
            updateOldIdsAndEmit(tuple); // add to old ids list and emit values
        else
            updateOldIdsAndReject(tuple); // remove from old ids list and reject tuple

        queue.add(JSONConverter.fromHashMapIntInt(oldIds));
    }

    /**
     * If lamp ID is just present in old ids list, it is removed and the
     * incoming tuple is rejected.
     *
     * @param tuple received
     */
    private void updateOldIdsAndReject(Tuple tuple) {
        Integer id = tuple.getIntegerByField(Constants.ID);

        if (oldIds.containsKey(id)) {
            oldIds.remove(id);
        }
    }

    /**
     * If lamp ID is not just present in the old ids list, this method
     * adds it to the list and emit values to next Bolt.
     *
     * @param tuple received
     */
    private void updateOldIdsAndEmit(Tuple tuple) {

        Integer id = tuple.getIntegerByField(Constants.ID);
        oldIds.put(id, 1);

        Values values = new Values();
        values.add(id);
        values.add(tuple.getValueByField(Constants.ADDRESS));
        values.add(tuple.getValueByField(Constants.LIFETIME));
        values.add(tuple.getValueByField(Constants.TIMESTAMP));

        collector.emit(tuple, values);
    }

    /**
     * Calculating if days from the last lamp replacement
     * indicating in the field "lifetime" are more than
     * LIFETIME_THRESHOLD to be considered quite old.
     *
     * @param tuple received
     * @return true if quite old, false otherwise
     */
    private boolean isOlderThan(Tuple tuple) {

        LocalDateTime lifetime = (LocalDateTime) tuple.getValueByField(Constants.LIFETIME);

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

    /**
     * @see MemcachedPeriodicUpdater
     */
    private class PeriodicUpdater extends MemcachedPeriodicUpdater {

        @Override
        public void run() {
            try {
                oldIds = this.cache.getIntIntMap(MemcachedManager.OLD_COUNTER);
            } catch (Exception e) {
                oldIds = new HashMap<>();
            }
        }
    }
}
