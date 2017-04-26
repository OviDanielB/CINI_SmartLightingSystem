package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.HeliosLog;
import org.uniroma2.sdcc.Utils.JSONConverter;
import org.uniroma2.sdcc.Utils.MOM.PubSubManager;
import org.uniroma2.sdcc.Utils.MOM.RabbitPubSubManager;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.util.Map;

/**
 * @author emanuele
 */
public class PrinterBolt extends BaseRichBolt {

    private static final String LOG_TAG = "[PrinterBolt]";

    private PubSubManager pubSubManager;

    /* topic based pub/sub routing key*/
    private final static String ROUTING_KEY = "dashboard.statistics.";

    private OutputCollector collector;

    /**
     * Bolt initialization
     *
     * @param map             map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        /* connect to rabbit , takes connection attributes from config file */
        this.pubSubManager = new RabbitPubSubManager();

        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        if (!TupleHelpers.isTickTuple(tuple)) {

            String toEmit = JSONConverter.fromTuple(tuple);

            HeliosLog.logOK(LOG_TAG,"Sent : " + toEmit);

            /* publish on rabbit queue with relative routing key */
            pubSubManager.publish(composeRoutingKey(toEmit),toEmit);
        }

        collector.ack(tuple);

    }

    /**
     * compose routing key based on received message
     * to differentiate consumption statistics on final
     * queue
     * @param toEmit message to send
     * @return relative routing key
     */
    private String composeRoutingKey(String toEmit) {
        String routingKey = ROUTING_KEY;

        if(toEmit.contains("id")){
            return routingKey + "lamps";
        } else if(toEmit.contains("*")){
            return routingKey + "global";
        } else {
            return routingKey + "streets";
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        /* nothing to declare, final bolt */
    }

}
