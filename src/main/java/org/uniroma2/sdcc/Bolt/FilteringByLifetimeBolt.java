package org.uniroma2.sdcc.Bolt;

import clojure.lang.Cons;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.ControlSystem.CentralController.ExecuteBolt;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
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
 * This Bolt processes arriving tuple from FilteringBolt
 * to reject which ones describe lamps with a value of lifetime
 * field smaller than a determinate LIFETIME_THRESHOLD
 */

public class FilteringByLifetimeBolt extends BaseRichBolt {

    private OutputCollector collector;
    /*  days threshold to be considered for ranking */
    private int lifetime_threshold;
    /*  number of old lamps  */
    private volatile HashMap<Integer, Integer> oldIds;
    private CacheManager cache;

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
//        this.cache = new MemcachedManager();
        this.cache.put(MemcachedManager.OLD_COUNTER, JSONConverter.fromHashMapIntInt(oldIds));
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        startPeriodicUpdate();
        startThread();
    }

    private void startPeriodicUpdate() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    oldIds = cache.getIntIntMap(MemcachedManager.OLD_COUNTER);
                } catch (Exception e) {
                    oldIds = new HashMap<>();
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 5000, 3000);
    }

    private void startThread() {
        executorService = Executors.newFixedThreadPool(3);
        IntStream.range(0, 3).forEach(e -> {
            executorService.submit(() -> {
                CacheManager manager = new MemcachedManager();
                try {
                    manager.put(MemcachedManager.OLD_COUNTER, queue.take());
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
        });
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

//        try {
//            oldIds = cache.getIntIntMap(MemcachedManager.OLD_COUNTER);
//        } catch (Exception e) {
//            this.oldIds = new HashMap<>();
//        }

        emitClassifiableLampTuple(tuple);

        collector.ack(tuple);
    }

    /**
     * Check and emit only tuple with value of lifetime > LIFETIME_THRESHOLD
     * and save id in memory if true.
     *
     * @param tuple received from FilteringBolt
     */
    private void emitClassifiableLampTuple(Tuple tuple) {

        if (isOlderThan(tuple))
            updateOldIdsAndEmit(tuple); // add to old ids list and emit values
        else
            updateOldIdsAndReject(tuple); // remove from old ids list and reject tuple

        //cache.put(MemcachedManager.OLD_COUNTER, JSONConverter.fromHashMapIntInt(oldIds));
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
}
