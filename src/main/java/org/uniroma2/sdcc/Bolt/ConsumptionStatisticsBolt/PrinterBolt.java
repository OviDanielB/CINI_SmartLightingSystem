package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.Config.RabbitConfig;
import org.uniroma2.sdcc.Utils.TupleHelpers;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author emanuele
 */
public class PrinterBolt extends BaseRichBolt {

    private final static String CONFIG_FILE = "./config/config.yml";
    private RabbitConfig rabbitConfig;

    /* rabbitMQ connection */
    private  final String  EXCHANGE_NAME = "dashboard_exchange";
    /* topic based pub/sub */
    private  final String EXCHANGE_TYPE = "topic";
    private  final String ROUTING_KEY = "dashboard.statistics.";
    private Connection connection;
    private Channel channel;

    public PrinterBolt() throws IOException {
        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner(CONFIG_FILE);
        rabbitConfig = yamlConfigRunner.getConfiguration().getQueue_out();
    }


    /**
     * Bolt initialization
     *
     * @param map map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        /* connect to rabbit */
        establishRabbitConnection();
    }

    @Override
    public void execute(Tuple tuple) {

        if (!TupleHelpers.isTickTuple(tuple)) {
            Gson gson = new Gson();
            String toEmit = gson.toJson(tuple);

            System.out.println("[CINI] [Printer] " + toEmit);

            try {

                // TODO improve message distinction
                if(toEmit.contains("id")) {
                    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY + "lamps", null, toEmit.getBytes());
                } else if(toEmit.contains("*")){
                    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY + "global", null, toEmit.getBytes());
                } else {
                    channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY + "streets", null, toEmit.getBytes());

                }
                System.out.println("[CINI][PrintBolt] Sent : " + toEmit);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }

    /**
     * connect to RabbitMQ to send statistics info to
     * dashboard
     */
    private void establishRabbitConnection() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitConfig.getHostname());
        factory.setPort(rabbitConfig.getPort());

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME,EXCHANGE_TYPE);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

}
