package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @author emanuele
 */
public class DailyAggregateConsumptionBolt extends AggregateConsumptionBolt {

    public DailyAggregateConsumptionBolt(int windowLengthInSeconds, int emitFrequencyInSeconds, Integer tickFrequencyInSeconds) {
        super(windowLengthInSeconds, emitFrequencyInSeconds);
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
    }


    protected boolean isValid(LocalDateTime timestamp) {
        LocalDateTime validTime = timestamp.truncatedTo(ChronoUnit.MINUTES);
        return timestamp.isEqual(validTime);
    }

}
