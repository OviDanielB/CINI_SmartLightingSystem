package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DailyIndividualConsumptionBolt extends IndividualConsumptionBolt {

    public DailyIndividualConsumptionBolt(Integer tickFrequencyInSeconds) {
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    public DailyIndividualConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    @Override
    protected boolean isValid(LocalDateTime timestamp) {
        LocalDateTime validTime = timestamp.truncatedTo(ChronoUnit.MINUTES);
        return timestamp.isEqual(validTime);
    }

}
