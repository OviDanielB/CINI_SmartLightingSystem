package org.uniroma2.sdcc;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.uniroma2.sdcc.Model.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

public class StreetLampDataSource {

    private final static String DEFAULT_ADDRESS_NAME = "Via del Politecnico";
    private final static String QUEUE_NAME = "storm";
    private final static float FAILURE_PROB = .3f;
    private final static String ADDRESS_FILE = "./address_list.txt";
    private static BufferedReader br;

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

            } catch (IOException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.run();


    }

    private static StreetLampMessage generateRandomStreetLight() {


        Address address = generateAddress();
        StreetLamp streetLamp = new StreetLamp();
        streetLamp.setAddress(address);
        streetLamp.setID(generateRandomInt());
        streetLamp.setLightIntensity(generateRandomFloat());
        streetLamp.setLampModel(Lamp.LED);
        streetLamp.setOn(randomMalfunctioning());
        streetLamp.setConsumption(generateRandomFloat());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        streetLamp.setLifetime(LocalDateTime.parse("12-12-2000 12:48:48", formatter));

        StreetLampMessage message = new StreetLampMessage();
        message.setNaturalLightLevel(generateRandomFloat());
        message.setStreetLamp(streetLamp);
        message.setTimestamp(System.currentTimeMillis());

        return message;
    }

    private static Address generateAddress() {
        Address address = new Address();
        try {
            if (br == null) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(ADDRESS_FILE)));
            }
            String line;
            while ((line = br.readLine()) == null) {
                br.close();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(ADDRESS_FILE)));
            }

            address.setName(line);

        } catch (IOException e) {
            e.printStackTrace();
            address.setName(DEFAULT_ADDRESS_NAME);
        }
        address.setNumber(generateRandomInt());
        address.setNumberType(AddressNumberType.CIVIC);
        return address;
    }

    private static boolean randomMalfunctioning() {
        float rand = (float) Math.random();
        return !(rand < FAILURE_PROB);
    }

    private static float generateRandomFloat() {

        return (float) (Math.random() * 100);
    }

    private static int generateRandomInt() {

        return (int) (Math.random() * 100);
    }
}
