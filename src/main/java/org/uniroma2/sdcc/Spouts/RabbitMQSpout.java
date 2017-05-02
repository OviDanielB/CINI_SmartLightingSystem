package org.uniroma2.sdcc.Spouts;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This Spout is the input to the System.
 * It consumes data produced by the sensors network on
 * the entry queue and send to the processing components.
 */
public class RabbitMQSpout extends BaseRichSpout {

    private SpoutOutputCollector outputCollector;

    private final static String QUEUE_NAME = "storm";
    private final static String HOSTNAME = "rabbit";
    private final static Integer PORT = 5672;

    private static String rabbitHost = HOSTNAME;
    private static Integer rabbitPort = PORT;
    private static String rabbitQueueName = QUEUE_NAME;

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
        MetricRegistry metrics = new MetricRegistry();
        Meter requests = metrics.meter("messages");

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

        configureRabbit();

        /* connect to rabbit */
        queue = new RabbitQueueManager(rabbitHost, rabbitPort, rabbitQueueName, QueueClientType.CONSUMER);
    }


    /**
     * configure rabbit connection parameters from config file
     */
    protected void configureRabbit() {

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {
            RabbitConfig rabbitConfig = yamlConfigRunner.getConfiguration().getQueue_in();
            rabbitHost = rabbitConfig.getHostname();
            rabbitPort = rabbitConfig.getPort();
            rabbitQueueName = rabbitConfig.getQueue_name();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
