package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.TupleHelpers;
import org.uniroma2.sdcc.Utils.WrappedKey;

import java.time.LocalDateTime;

/**
 * @author emanuele
 */
public class HourlyConsumptionBolt extends ConsumptionBolt {

    public HourlyConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
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
            logger.info(String.format("Received tick tuple at %d", System.currentTimeMillis()));
            emitCurrentWindowAvgs(window);

        } else {

            WrappedKey key = getStatisticsKey(tuple);
            LocalDateTime timestamp = (LocalDateTime) tuple.getValueByField("timestamp");
            Float consumption = tuple.getFloatByField("consumption");

            try {

                window.updatedConsumptionAvg(key, consumption, timestamp);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                logger.debug(e.getMessage());
            }
        }
    }


}
