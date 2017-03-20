package org.uniroma2.sdcc;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class StreetLampDataSource {

    private static String QUEUE_NAME = "storm";

    private static void rabbitProducer() {

        Thread producer = new Thread(() -> {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection;
            try {
                connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                StreetLamp streetLamp;
                Gson gson = new Gson();
                String message;
                while (true) {
                    streetLamp = generateRandomStreetLight();
                    message = gson.toJson(streetLamp);
                    channel.basicPublish("", "storm", null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                    Thread.sleep(1000);
                }

            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.run();


    }

    private static StreetLamp generateRandomStreetLight() {
        Address address = new Address();
        address.setName("Via del Politecnico");
        address.setNumber(generateRandomInt());
        address.setNumberType(AddressNumberType.CIVIC);

        StreetLamp data = new StreetLamp();
        data.setAddress(address);
        data.setID(generateRandomInt());
        data.setLightIntensity(generateRandomFloat());
        data.setLampModel(Lamp.LED);
        data.setOn(true);
        data.setConsumption(generateRandomFloat());
        data.setLifetime("13/02/2016");

        return data;
    }


    private static float generateRandomFloat() {
        return (float) (Math.random() * 100);
    }

    private static int generateRandomInt() {
        return (int) (Math.random() * 100000);
    }

    public static void main(String[] args) {
        rabbitProducer();
    }
}