package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.TupleHelpers;
import org.uniroma2.sdcc.Utils.WrappedKey;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Bolt produces statistics on consumption field of incoming tuple.
 * Every emitFrequencyInSeconds the computed statistics on the window is emitted.
 *
 * @author emanuele
 * @see SlidingWindowBolt
 */
public class IndividualConsumptionBolt extends SlidingWindowBolt<WrappedKey> {

    protected Integer tickCount = 0;

    public IndividualConsumptionBolt(int windowLengthInSeconds, int tickFrequencyInSeconds) {
        super(windowLengthInSeconds, tickFrequencyInSeconds, tickFrequencyInSeconds);
    }

    @Override
    public void execute(Tuple tuple) {

        /*
         * If bolt receives a tick tuple
         * (a) slide window
         * (b) emit statistics
         *
         * If bolt receives a data tuple
         * (a) Update statistics
         */

         /*
         * if tick tuple then increment counter.
         * If counter is emitFrequencyInSeconds/tickFrequency then emit.
         *
         * Otherwise getString timestamp for incoming tuple. If it corresponds to
         * a fraction time (for example 10:00, 14:00 and not 10:04 or 14:26)
         * insert it in the sliding window.
         *
         * The sliding window has 24 slot. The first keep statistics for data with timestamp
         * 00:00 to 00:59 etc.
         */
        if (TupleHelpers.isTickTuple(tuple)) {

            tickCount++;
            if (tickCount == (emitFrequencyInSeconds / tickFrequencyInSeconds)) {
                emitCurrentWindowAvgs(window, tuple);
                tickCount = 0;
            }

        } else {

            LocalDateTime timestamp = (LocalDateTime) tuple.getValueByField(Constants.TIMESTAMP);
            Float hourAvg = tuple.getFloatByField(Constants.CONSUMPTION);
            WrappedKey key = getStatisticsKey(tuple);

            /*
             * example : In the slot 19:00 / 20:00 we can put only
             * tuple with timestamp from 19:00 to (i.e) 19:05
             * Here we have to filter
             */
            if (isValid(timestamp))
                window.updatedConsumptionAvg(key, hourAvg, timestamp);

        }

        collector.ack(tuple);
    }

    /**
     * Slide window and emit computer statistics.
     * Retrieve actual windows-length that in the starting phase can be different from the specified one.
     *
     * @see this#emit(Map, LocalDateTime, int, Tuple)
     */
    protected void emitCurrentWindowAvgs(SlidingWindowAvg slidingWindow, Tuple tuple) {

        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
        lastModifiedTracker.markAsModified();

        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
            logger.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
        }

        Map<WrappedKey, Float> statistics = slidingWindow.getAvgThenAdvanceWindow();
        emit(statistics, slidingWindow.getLastSlide(), actualWindowLengthInSeconds, tuple);
    }

    /**
     * Emit data in the OutputCollector
     *
     * @param stat                        statistics to emit
     * @param windowEnd                   Instant of time at which statistics computation ends and start new window
     * @param actualWindowLengthInSeconds window Length ( windows end - window start )
     */
    protected void emit(Map<WrappedKey, Float> stat, LocalDateTime windowEnd, int actualWindowLengthInSeconds, Tuple tuple) {

        System.out.println("[CINI] Intermediate hourly statistics at " + windowEnd + "\n\n");
        for (WrappedKey key : stat.keySet()) {
            Float avg = stat.get(key);
            collector.emit(tuple, new Values(key.getId(), key.getStreet(), avg, windowEnd,
                    actualWindowLengthInSeconds));
        }
    }

    protected WrappedKey getStatisticsKey(Tuple tuple) {
        WrappedKey key = new WrappedKey();
        key.setId(tuple.getIntegerByField(Constants.ID));
        key.setStreet(tuple.getStringByField(Constants.ADDRESS));

        return key;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.ID,
                Constants.ADDRESS,
                Constants.CONSUMPTION,
                Constants.TIMESTAMP,
                Constants.WINDOW_LENGTH));
    }

    protected boolean isValid(LocalDateTime timestamp) {
        return true;
    }

    public Integer getTickCount() {
        return tickCount;
    }

    public void setTickCount(Integer tickCount) {
        this.tickCount = tickCount;
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        // configure how often a tick tuple will be sent to our bolt
        return TupleHelpers.getTickTupleFrequencyConfig(tickFrequencyInSeconds);
    }
}
