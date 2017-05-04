package org.uniroma2.sdcc.Spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.MOM.QueueManger;
import org.uniroma2.sdcc.Utils.MOM.RabbitQueueManager;

import java.util.Map;

/**
 * This Spout is the input to the System.
 * It consumes data produced by the sensors network on
 * the entry queue and send to the processing components.
 */
public class RabbitMQSpout extends BaseRichSpout {

    private SpoutOutputCollector outputCollector;

    /* input queue */
    private QueueManger queue;

    public RabbitMQSpout() {
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        outputCollector = collector;
        prepareRabbitConnection();
    }

    @Override
    public void nextTuple() {

        /* blocking operation */
        String mess = queue.nextMessage();

        /* emit anchored tuple with hasCode as ID */
        outputCollector.emit(new Values(mess), mess.hashCode());

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(Constants.JSON_STRING));

    }

    @Override
    public void close() {
        /* close connection resources */
        queue.close();
    }


    /**
     * Connect to RabbitMQ to consume message from queue.
     */
    private void prepareRabbitConnection() {

        /* connect to rabbit */
        queue = new RabbitQueueManager();
    }

}
