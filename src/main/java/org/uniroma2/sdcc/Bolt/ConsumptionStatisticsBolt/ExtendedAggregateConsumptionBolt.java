package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Bolt produces statistics on consumption field of incoming tuple.
 * Statistics are computed for street and on a sliding window with length that is a
 * multiple of the AggregateConsumptionBolt window's length.
 *
 * @author emanuele
 * @see SlidingWindowBolt
 * @see AggregateConsumptionBolt
 */
public class ExtendedAggregateConsumptionBolt extends AggregateConsumptionBolt {

    public ExtendedAggregateConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, tickFrequencyInSeconds);
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;
    }

    /**
     * Consider only tuple with timestamp of the type hh:mm:00.
     *
     * @param timestamp of the incoming tuple
     * @return true if the tuple must be considered in the statistics.
     */
    @Override
    protected boolean isValid(LocalDateTime timestamp) {
        LocalDateTime validTime = timestamp.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime ts = timestamp.truncatedTo(ChronoUnit.SECONDS);
        return ts.isEqual(validTime);
    }

}
