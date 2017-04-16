package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

/**
 * Bolt to adjust incoming tuple to be emit.
 *
 * @author emanuele
 */
public class ParserBolt extends BaseRichBolt {

    private OutputCollector collector;


    /**
     * Bolt initialization
     *
     * @param map map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        if (!TupleHelpers.isTickTuple(tuple)) {

            Integer id = tuple.getIntegerByField(Constants.ID);
            Address address = (Address) tuple.getValueByField(Constants.ADDRESS);
            Float consumption = tuple.getFloatByField(Constants.CONSUMPTION);
            Long timestamp = tuple.getLongByField(Constants.TIMESTAMP);
            LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                    .getDefault().toZoneId());

            collector.emit(new Values(id, address.getName(), consumption, ts));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id", "street", "consumption", "timestamp"));
    }

}
