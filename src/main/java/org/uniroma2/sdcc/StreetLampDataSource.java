package org.uniroma2.sdcc;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class StreetLampDataSource
{

    private static String QUEUE_NAME = "storm";

    private static void rabbitProducer() {

        Thread producer = new Thread(() -> {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = null;
            try {
                connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                StreetLampMessage streetLamp ;
                Gson gson = new Gson();
                String message;
                while(true) {
                    streetLamp = generateRandomStreetLight();
                    message = gson.toJson(streetLamp);
                    channel.basicPublish("", "storm", null, message.getBytes());
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
        });

        producer.run();



    }

    private static StreetLampMessage generateRandomStreetLight() {
        Address address = new Address();
        address.setName("Via del Politecnico");
        address.setNumber(generateRandomInt());
        address.setNumberType(AddressNumberType.CIVIC);

        StreetLamp streetLamp = new StreetLamp();
        streetLamp.setAddress(address);
        streetLamp.setID(generateRandomInt());
        streetLamp.setLightIntensity(generateRandomFloat());
        streetLamp.setLampModel(Lamp.LED);
        streetLamp.setOn(true);
        streetLamp.setConsumption(generateRandomFloat());

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        DateTime dt = formatter.parseDateTime("13/02/2016");
        streetLamp.setLifetime(dt);
        
        StreetLampMessage message = new StreetLampMessage();
        message.setNaturalLight(new NaturalLight(generateRandomFloat()));
        message.setStreetLamp(streetLamp);
        message.setTimestamp(System.currentTimeMillis());

        return message;
    }

    private static float generateRandomFloat() {
        float rand =(float) (Math.random() * 100);
        return rand;
    }

    private static int generateRandomInt() {
        int rand = (int) (Math.random() * 100000);
        return rand;
    }

    public static void main( String[] args ) {
        rabbitProducer();
    }
}
