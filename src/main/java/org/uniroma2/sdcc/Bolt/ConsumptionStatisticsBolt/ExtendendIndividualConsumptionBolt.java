package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Bolt produces statistics. SlidingWindow is set on one day. Every emitFrequencyInSeconds the
 * updated window is emitted. Statistics are computed for streetLamp.
 *
 * @author emanuele
 * @see SlidingWindowBolt
 */
public class ExtendendIndividualConsumptionBolt extends IndividualConsumptionBolt {

    public ExtendendIndividualConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, tickFrequencyInSeconds);
        // emitFrequencyInSeconds = tickFrequencyInSeconds to default
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
        return timestamp.isEqual(validTime);
    }

}
