package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.TimeZone;

public class DailyConsumptionBolt extends WindowSlidingStatisticsBolt {

    private Integer lagInSeconds = 5;              // accept interval for tuple's timestamp
    private Integer tickFrequencyInSeconds;        // tick tuple arrival frequency
    private Integer tickCount = 0;


    public DailyConsumptionBolt(Integer lagInSeconds, Integer tickFrequencyInSeconds) {
        this.lagInSeconds = lagInSeconds;
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }

    public DailyConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer lagInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
        this.lagInSeconds = lagInSeconds;
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
                emitCurrentWindowAvgs(statStreetLamp, STREETLAMP_DISC);
                tickCount = 0;
            }

        } else {

            Long timestamp = tuple.getLongByField("end window");
            Float hourAvg = tuple.getFloatByField("avg");
            String id = tuple.getStringByField("key");
            String discriminator = tuple.getStringByField("discriminator");

            /*
             * example : In the slot 19:00 / 20:00 we can put only
             * tuple with timestamp from 19:00 to (i.e) 19:05
             * Here we have to filter
             */
            if (isValid(timestamp)) {
                if (Objects.equals(discriminator, STREETLAMP_DISC))
                    statStreetLamp.updatedConsumptionAvg(id, hourAvg, timestamp);
                else if (Objects.equals(discriminator, STREET_DISC))
                    statStreet.updatedConsumptionAvg(id, hourAvg, timestamp);

            }

        }
    }

    protected boolean isValid(Long timestamp) {
        LocalTime ts = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TimeZone
                .getDefault().toZoneId()).toLocalTime();

        LocalTime validLimitTime = ts.truncatedTo(ChronoUnit.MINUTES).plusSeconds(lagInSeconds);
        if (ts.isAfter(validLimitTime))
            return false;

        return true;
    }

}
