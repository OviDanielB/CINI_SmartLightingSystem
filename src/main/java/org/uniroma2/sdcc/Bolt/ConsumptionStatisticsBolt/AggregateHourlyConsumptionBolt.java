package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * @author emanuele
 */
public class AggregateHourlyConsumptionBolt extends AggregateConsumptionBolt {

    public AggregateHourlyConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
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
        if (TupleHelpers.isTickTuple(tuple)) {
            logger.info(String.format("Received tick tuple at %d , triggering emit of current window counts",
                    System.currentTimeMillis()));
            emitCurrentWindowAvgs(window);

        } else {

            String key = getStatisticsKey(tuple);
            Long timestamp = tuple.getLongByField("timestamp");
            Float consumption = tuple.getFloatByField("consumption");

            try {
                LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                        .getDefault().toZoneId());
                window.updatedConsumptionAvg(key, consumption, ts);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                logger.debug(e.getMessage());
            }
        }
    }

}
