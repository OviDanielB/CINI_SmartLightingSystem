package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;


import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.TupleHelpers;

public class HourlyConsumptionBolt extends WindowSlidingStatisticsBolt {

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
            logger.info("Received tick tuple, triggering emit of current window counts");
            emitCurrentWindowAvgs(statStreetLamp, STREETLAMP_DISC);
            emitCurrentWindowAvgs(statStreet, STREET_DISC);

        } else {

            String id = tuple.getValueByField(Constant.ID).toString();
            Address address = (Address) tuple.getValueByField(Constant.ADDRESS);
            String street = address.getName();
            Long timestamp = tuple.getLongByField(Constant.TIMESTAMP);
            Float consumption = tuple.getFloatByField(Constant.CONSUMPTION);

            try {
                statStreetLamp.updatedConsumptionAvg(id, consumption, timestamp / 1000);
                statStreet.updatedConsumptionAvg(street, consumption, timestamp / 1000);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                logger.debug(e.getMessage());
            }

        }
    }
}