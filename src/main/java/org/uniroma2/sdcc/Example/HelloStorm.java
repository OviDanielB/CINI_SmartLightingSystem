package org.uniroma2.sdcc.Example;

import com.rabbitmq.client.*;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.shade.org.yaml.snakeyaml.Yaml;
import org.apache.storm.topology.TopologyBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ovidiudanielbarba on 07/03/2017.
 */
public class HelloStorm {

    public static void main(String[] args) throws Exception{
        Config config = new Config();
        config.put("inputFile", args[0]);
        config.setDebug(true);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("line-reader-spout", new LineReaderSpout());
        builder.setBolt("word-spitter", new WordSpitterBolt()).shuffleGrouping("line-reader-spout");
        builder.setBolt("word-counter", new WordCounterBolt()).shuffleGrouping("word-spitter");



        /*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("HelloStorm", config, builder.createTopology());
        Thread.sleep(10000);

        cluster.shutdown();

*/

        Yaml yaml = new Yaml();
        Object v = yaml.load(new FileInputStream(new File("config/config.yml")));
        System.out.println(v.toString());


        /* rabbitmq example
        * first type in terminal (to activate message broker server):
        *  docker run -d -p 5672:5672 -p 15672:15672  --name rabbitmq rabbitmq:3.6.6
        */
        rabbitConsumer();
        rabbitProducer();

        //StormSubmitter.submitTopology("Hello Storm",config,builder.createTopology());


    }

    private static void rabbitConsumer() {

        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = null;
                Channel channel = null;
                try {
                    connection = factory.newConnection();
                    channel = connection.createChannel();

                    channel.queueDeclare("hello", false, false, false, null);

                    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }


                Consumer consumer = new DefaultConsumer(channel){
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [x] Received '" + message + "'");
                    }
                };
                try {
                    channel.basicConsume("hello", true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        consumer.run();
    }

    private static void rabbitProducer() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare("hello", false, false, false, null);
            String message = "Hello World!";
            while(true) {
                channel.basicPublish("", "hello", null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}