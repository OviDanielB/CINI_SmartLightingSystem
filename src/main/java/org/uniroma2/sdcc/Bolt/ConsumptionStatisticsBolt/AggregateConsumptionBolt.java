package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author emanuele
 */
public abstract class AggregateConsumptionBolt extends SlidingWindowBolt<String> {

    public AggregateConsumptionBolt() {
        super();
    }

    public AggregateConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
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

        Map<String, Float> statistics = slidingWindow.getAvgThenAdvanceWindow();
        emit(statistics, slidingWindow.getLastSlide(), actualWindowLengthInSeconds);
    }

    /**
     * Emit data in the OutputCollector
     *
     * @param stat                        statistics to emit
     * @param windowEnd                   Instant of time at which statistics computation ends and start new window
     * @param actualWindowLengthInSeconds window Length ( windows end - window start )
     */
    protected void emit(Map<String, Float> stat, LocalDateTime windowEnd, int actualWindowLengthInSeconds) {

        for (String key : stat.keySet()) {
            Float avg = stat.get(key);
            System.out.println(key + " avg: " + avg + " in window ending at " + windowEnd + "\n");
            collector.emit(new Values(key, avg, windowEnd, actualWindowLengthInSeconds));
        }
    }

    protected String getStatisticsKey(Tuple tuple) {
        return tuple.getStringByField("street");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("street", "consumption", "timestamp", "window length"));
    }

}
