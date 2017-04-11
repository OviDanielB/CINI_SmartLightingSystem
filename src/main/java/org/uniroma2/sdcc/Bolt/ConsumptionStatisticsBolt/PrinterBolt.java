package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.util.Map;

/**
 * @author emanuele
 */
public class PrinterBolt extends BaseRichBolt {


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
    }

    @Override
    public void execute(Tuple tuple) {

        if (!TupleHelpers.isTickTuple(tuple)) {
            Gson gson = new Gson();
            String toEmit = gson.toJson(tuple);

            System.out.println(toEmit);
            /* todo put in queue*/
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
