package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Utils.RabbitConfig;
import org.uniroma2.sdcc.Utils.TupleHelpers;
import org.uniroma2.sdcc.Utils.YamlConfigRunner;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author emanuele
 */
public class PrinterBolt extends BaseRichBolt {

    private final static String CONFIG_FILE = "./config/config.yml";

    private Channel channel;
    private RabbitConfig rabbitConfig;

    public PrinterBolt() throws IOException {
        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner(CONFIG_FILE);
        rabbitConfig = yamlConfigRunner.getConfiguration().getQueue_out();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitConfig.getHostname());
        factory.setPort(rabbitConfig.getPort());
        Connection connection;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(rabbitConfig.getQueue_name(), false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void execute(Tuple tuple) {

        if (!TupleHelpers.isTickTuple(tuple)) {
            Gson gson = new Gson();
            String toEmit = gson.toJson(tuple);

            System.out.println(toEmit);

            try {
                channel.basicPublish("", rabbitConfig.getQueue_name(), null, toEmit.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }

}
