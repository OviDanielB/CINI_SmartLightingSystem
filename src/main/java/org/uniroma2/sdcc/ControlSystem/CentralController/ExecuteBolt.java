package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Model.Lamp;
import org.uniroma2.sdcc.Model.StreetLamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ExecuteBolt extends BaseRichBolt{

    private OutputCollector collector;
    private Gson gson;
    private static final String LOG_TAG = "[CINI] [ExecuteBolt] ";

    /* rabbitMQ connection */
    private final static String RABBIT_HOST = "localhost";
    private final static Integer RABBIT_PORT = 5673;
    private  static final String  EXCHANGE_NAME = "control_exchange";
    /* topic based pub/sub */
    private  static final String EXCHANGE_TYPE = "topic";
    private  static final String ROUTING_KEY = "control.adapt";
    private Connection connection;
    private Channel channel;




    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = collector;
        this.gson = new Gson();

        establishRabbitConnection();
    }

    @Override
    public void execute(Tuple tuple) {
        /* riceve la tupla contenente il valore di intensità della strada ottimale */
        /* pubblica su pub/sub il valore */

        int id =                    (int) tuple.getValueByField(AnomalyStreetLampMessage.ID);
        Address address =           (Address) tuple.getValueByField(AnomalyStreetLampMessage.ADDRESS);
        Lamp model =                (Lamp) tuple.getValueByField(AnomalyStreetLampMessage.LAMP_MODEL);
        Float consumption =         (Float) tuple.getValueByField(AnomalyStreetLampMessage.CONSUMPTION);
        LocalDateTime lifetime =    (LocalDateTime) tuple.getValueByField(AnomalyStreetLampMessage.LIFETIME);
        Float adapted_intensity =   (Float) tuple.getValueByField(Constant.ADAPTED_INTENSITY);


        StreetLamp adapted_lamp = new StreetLamp(
                id, true, model, address, consumption, adapted_intensity, lifetime);

        String json_adapted_lamp = gson.toJson(adapted_lamp);

        try {

            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, json_adapted_lamp.getBytes());

            System.out.println(LOG_TAG + "Sent : " + json_adapted_lamp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        collector.ack(tuple);
    }

    /**
     * Connect to RabbitMQ to send data to adapt lamp
     */
    private void establishRabbitConnection() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT_HOST);
        factory.setPort(RABBIT_PORT);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME,EXCHANGE_TYPE);
            System.out.println(LOG_TAG + "Rabbit connection established on " + RABBIT_HOST + "/" + RABBIT_PORT);

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            System.out.println(LOG_TAG + "Rabbit Connection Failed.");
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
