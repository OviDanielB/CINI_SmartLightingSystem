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
import org.uniroma2.sdcc.Model.StreetLampMessage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RabbitMQSpout extends BaseRichSpout {

    private Connection connection;
    private Channel channel;
    private ConnectionFactory connectionFactory;
    private SpoutOutputCollector outputCollector;
    private Consumer consumer;

    /* measure requests/second */
    private MetricRegistry metrics ;
    private Meter requests;

    // TODO Remove
    private List<String> messageQueue;

    private static String QUEUE_NAME = "storm";



    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        outputCollector = collector;
        messageQueue = new ArrayList<>();

        prepareMetrics();
        prepareRabbitConnection();

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

    private void prepareRabbitConnection() {

        /* RabbitMQ example
        *  First type in terminal (to activate message broker server):
        *  'docker run -d --hostname my-rabbit -p 5672:5672 -p 15672:15672  --name rabbitmq -e
        *  RABBITMQ_ERLANG_COOKIE='storm' rabbitmq:3.6.6'
        *
        * Enable Web UI in rabbitmq container, available in localhost:15672
        * 'docker exec rabbitmq rabbitmq-plugins enable rabbitmq_management'
        *
        *
        *  Web UI available on localhost:8080
        *  'docker run -d --hostname my-rabbit --name rabbit_m -p 8080:15672 -e RABBITMQ_ERLANG_COOKIE='storm'
        *  rabbitmq:3.6.6-management'
        */

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");


        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            /* GET QUEUE MESSAGE COUNT with declareOk.getMessageCount() -> int */
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME,false,false,false,null);
//            System.out.println("[CINI] RabbitMQSpout waiting for messages. To exit press CTRL+C");

            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    /* contribute to metrics */
                    requests.mark();

                    /* convert mess byte array to string*/
                    String message = new String(body, "UTF-8");
//                    System.out.println("[CINI] RabbitMQSpout received '" + message + "'");
                    messageQueue.add(message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
//                    System.out.println("[CINI] QUEUE MESSAGGE COUNT : " + declareOk.getMessageCount());

                    //System.out.println("[CINI] Rabbit : " + message);

                }
            };

            try {
                // autoAck = false => send automatic ack
                channel.basicConsume(QUEUE_NAME, false, consumer);


            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
//            System.out.println("[CINI] Rabbit Connection Failed");
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }



    }


    @Override
    public void nextTuple() {
        if(messageQueue.size() == 0){
            return;
        }

//        System.out.println("[CINI] MessaggeQueue size = " + messageQueue.size() + "\n");

        String mess = messageQueue.get(0);
        messageQueue.remove(0);
        outputCollector.emit(new Values(mess));


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(StreetLampMessage.JSON_STRING));

    }

    @Override
    public void close() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
