package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.TupleHelpers;
import org.uniroma2.sdcc.Utils.WrappedKey;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class DailyConsumptionBolt extends ConsumptionBolt {

    private Integer tickFrequencyInSeconds;        // tick tuple arrival frequency
    private Integer tickCount = 0;

    public DailyConsumptionBolt(Integer tickFrequencyInSeconds) {
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    public DailyConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    @Override
    public void execute(Tuple tuple) {

         /*
         * if tick tuple then increment counter.
         * If counter is emitFrequencyInSeconds/tickFrequency then emit.
         *
         * Otherwise get timestamp for incoming tuple. If it corresponds to
         * a fraction time (for example 10:00, 14:00 and not 10:04 or 14:26)
         * insert it in the sliding window.
         *
         * The sliding window has 24 slot. The first keep statistics for data with timestamp
         * 00:00 to 00:59 etc.
         */
        if (TupleHelpers.isTickTuple(tuple)) {

            tickCount++;
            if (tickCount == (emitFrequencyInSeconds / tickFrequencyInSeconds)) {
                emitCurrentWindowAvgs(window);
                tickCount = 0;
            }

        } else {

            LocalDateTime timestamp = (LocalDateTime) tuple.getValueByField(Constant.TIMESTAMP);
            Float hourAvg = tuple.getFloatByField(Constant.CONSUMPTION);
            WrappedKey key = getStatisticsKey(tuple);

            /*
             * example : In the slot 19:00 / 20:00 we can put only
             * tuple with timestamp from 19:00 to (i.e) 19:05
             * Here we have to filter
             */
            if (isValid(timestamp))
                window.updatedConsumptionAvg(key, hourAvg, timestamp);

        }
    }

    protected boolean isValid(LocalDateTime timestamp) {
        LocalDateTime validTime = timestamp.truncatedTo(ChronoUnit.MINUTES);
        return timestamp.isEqual(validTime);
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


}
