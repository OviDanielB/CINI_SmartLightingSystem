package org.uniroma2.sdcc.Spouts;

import com.rabbitmq.client.*;
import org.apache.storm.shade.com.codahale.metrics.ConsoleReporter;
import org.apache.storm.shade.com.codahale.metrics.Meter;
import org.apache.storm.shade.com.codahale.metrics.MetricRegistry;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.Config.RabbitConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;
import org.uniroma2.sdcc.Utils.MOM.QueueClientType;
import org.uniroma2.sdcc.Utils.MOM.QueueManger;
import org.uniroma2.sdcc.Utils.MOM.RabbitQueueManager;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This Spout is the input to the System.
 * It consumes data produced by the sensors network on
 * the entry queue and send to the processing components.
 */
public class RabbitMQSpout extends BaseRichSpout {

    private SpoutOutputCollector outputCollector;

    /* measure requests/second */
    private MetricRegistry metrics;
    private Meter requests;
    
    private QueueManger queue;


    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        outputCollector = collector;

        prepareRabbitConnection();

    }

    @Override
    public void nextTuple() {

        /* blocking operation */
        String mess = queue.nextMessage();

        /* emit anchored tuple */
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

    private void prepareMetrics() {
        metrics = new MetricRegistry();
        requests = metrics.meter("messages");

        /* starts reporting every second requests/sec */
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(60, TimeUnit.SECONDS);
    }

    /**
     * Connect to RabbitMQ to consume message from queue.
     */
    private void prepareRabbitConnection() {

        /* connect to rabbit */
        queue = new RabbitQueueManager();
    }

}
