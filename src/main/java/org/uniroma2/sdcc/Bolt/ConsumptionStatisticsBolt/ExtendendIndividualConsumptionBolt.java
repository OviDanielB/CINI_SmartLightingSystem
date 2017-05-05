package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Bolt produces statistics on consumption field of incoming tuple.
 * This class computes statistics into a sliding window that has a length equals to a
 * multiple of the IndividualConsumptionBolt one.
 *
 * @author emanuele
 * @see SlidingWindowBolt
 * @see IndividualConsumptionBolt
 */
public class ExtendendIndividualConsumptionBolt extends IndividualConsumptionBolt {

    public ExtendendIndividualConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
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
