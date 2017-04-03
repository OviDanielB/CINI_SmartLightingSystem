package org.uniroma2.sdcc;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class StreetLampDataSource {

    private static String QUEUE_NAME = "storm";
    private static float FAILURE_PROB = .3f;

    public static void main(String[] args) {

        rabbitProducer();
    }

    private static void rabbitProducer() {

        Thread producer = new Thread(() -> {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = null;
            try {
                connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                StreetLampMessage streetLamp;
                Gson gson = new Gson();
                String message;
                while (true) {
                    streetLamp = generateRandomStreetLight();
                    message = gson.toJson(streetLamp);
                    channel.basicPublish("", "storm", null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");


                    Thread.sleep(100);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.run();


    }

    private static StreetLampMessage generateRandomStreetLight() {
        Address address = new Address();
        address.setAddressType(AddressType.STREET);
        address.setName("Politecnico");
        address.setNumber(generateRandomInt());
        address.setNumberType(AddressNumberType.CIVIC);

        StreetLamp streetLamp = new StreetLamp();
        streetLamp.setAddress(address);
        streetLamp.setID(generateRandomInt());
        streetLamp.setLightIntensity(generateRandomFloat());
        streetLamp.setLampModel(Lamp.LED);
        streetLamp.setOn(randomMalfunctioning());
        streetLamp.setConsumption(generateRandomFloat());
        streetLamp.setLifetime(new Date(231211310));

        StreetLampMessage message = new StreetLampMessage();
        message.setNaturalLightLevel(new NaturalLightLevel(generateRandomFloat()));
        message.setStreetLamp(streetLamp);
        message.setTimestamp(System.currentTimeMillis());

        return message;
    }

    private static boolean randomMalfunctioning() {
        float rand = (float) Math.random();
        if (rand < FAILURE_PROB) {
            return false;
        }

        return true;
    }

    private static float generateRandomFloat() {

        float rand = (float) (Math.random() * 100);
        return rand;
    }

    private static int generateRandomInt() {

        int rand = (int) (Math.random() * 100000);
        return rand;
    }
}
