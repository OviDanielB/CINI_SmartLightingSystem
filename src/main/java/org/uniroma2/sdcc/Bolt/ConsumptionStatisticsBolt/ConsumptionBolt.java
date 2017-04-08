package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.WrappedKey;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author emanuele
 */
public abstract class ConsumptionBolt extends SlidingWindowBolt<WrappedKey> {


    public ConsumptionBolt() {
    }

    public ConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
    }

    /**
     * Slide window and emit computer statistics.
     * Retrieve actual windows-length that in the starting phase can be different from the specified one.
     *
     * @see this#emit(Map, LocalDateTime, int)
     */
    protected void emitCurrentWindowAvgs(SlidingWindowAvg slidingWindow) {

        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
        lastModifiedTracker.markAsModified();

        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
            logger.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
        }

        Map<WrappedKey, Float> statistics = slidingWindow.getAvgThenAdvanceWindow();
        emit(statistics, slidingWindow.getLastSlide(), actualWindowLengthInSeconds);
    }

    /**
     * Emit data in the OutputCollector
     *
     * @param stat                        statistics to emit
     * @param windowEnd                   Instant of time at which statistics computation ends and start new window
     * @param actualWindowLengthInSeconds window Length ( windows end - window start )
     */
    protected void emit(Map<WrappedKey, Float> stat, LocalDateTime windowEnd, int actualWindowLengthInSeconds) {

        System.out.println("[CINI] Intermediate hourly statistics at " + windowEnd + "\n\n");
        for (WrappedKey key : stat.keySet()) {
            Float avg = stat.get(key);
            System.out.println(key.getId() + " avg: " + avg);
            collector.emit(new Values(key.getId(), key.getStreet(), avg, windowEnd,
                    actualWindowLengthInSeconds));
        }
    }

    protected WrappedKey getStatisticsKey(Tuple tuple) {
        WrappedKey key = new WrappedKey();
        key.setId(tuple.getIntegerByField("id"));
        key.setStreet(tuple.getStringByField("street"));

        return key;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id", "street", "consumption",
                "timestamp", "window length"));
    }

}
