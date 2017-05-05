package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Bolt produces statistics on consumption field of incoming tuple.
 * It allows to compute statistics on aggregate af street-lamp.
 *
 * @author emanuele
 * @see SlidingWindowBolt
 */
public class AggregateConsumptionBolt extends SlidingWindowBolt<String> {

    protected Integer tickCount = 0;   // tick count to emit at multiple of tickFrequencyInSeconds

    public AggregateConsumptionBolt(int windowLengthInSeconds, int tickFrequencyInSeconds) {
        super(windowLengthInSeconds, tickFrequencyInSeconds, tickFrequencyInSeconds);
    }

    @Override
    public void execute(Tuple tuple) {

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
            String key = getStatisticsKey(tuple);

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
     * @see this#emit(Map, LocalDateTime, int)
     */
    protected void emitCurrentWindowAvgs(SlidingWindowAvg slidingWindow, Tuple tuple) {

        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
        lastModifiedTracker.markAsModified();

        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
            logger.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
        }

        Map<String, Float> statistics = slidingWindow.getAvgThenAdvanceWindow();
        emit(statistics, slidingWindow.getLastSlide(), actualWindowLengthInSeconds);

        Float globalStat = slidingWindow.getTotalAvg();
        collector.emit(tuple, new Values("*", globalStat, slidingWindow.getLastSlide(), actualWindowLengthInSeconds));

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
            collector.emit(new Values(key, avg, windowEnd, actualWindowLengthInSeconds));
        }
    }

    protected String getStatisticsKey(Tuple tuple) {
        return tuple.getStringByField(Constants.ADDRESS);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.ADDRESS,
                Constants.CONSUMPTION,
                Constants.TIMESTAMP,
                Constants.WINDOW_LENGTH));
    }

    protected boolean isValid(LocalDateTime timestamp) {
        return true;
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        // configure how often a tick tuple will be sent to our bolt
        return TupleHelpers.getTickTupleFrequencyConfig(tickFrequencyInSeconds);
    }

    public Integer getTickCount() {
        return tickCount;
    }

    public void setTickCount(Integer tickCount) {
        this.tickCount = tickCount;
    }
}
