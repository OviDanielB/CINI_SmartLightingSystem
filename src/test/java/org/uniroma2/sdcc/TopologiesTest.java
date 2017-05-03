package org.uniroma2.sdcc;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.HeliosLog;
import org.uniroma2.sdcc.Utils.JSONConverter;
import org.uniroma2.sdcc.Utils.MOM.QueueClientType;
import org.uniroma2.sdcc.Utils.MOM.RabbitPubSubManager;
import org.uniroma2.sdcc.Utils.MOM.RabbitQueueManager;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Test all topology operation sending testing values on input queue and
 * comparing values received on output queue with the expected ones.
 * NB: Topology to test must be running.
 */
public class TopologiesTest {

    private static final String LOG_TAG = "[HELIOS][Topology Test] ";

    /* rabbit topology entry connection */
    private static Connection connectionEntry;
    private static Channel channelEntry;
    private static final String RABBIT_ENTRY_HOST = "localhost";
    private static final Integer RABBIT_ENTRY_PORT = 5672;
    private static final String RABBIT_ENTRY_QUEUE_NAME = "test";
//    RabbitQueueManager rabbitQueueManagerEntry =
//            new RabbitQueueManager(RABBIT_ENTRY_HOST, RABBIT_ENTRY_PORT, RABBIT_ENTRY_QUEUE_NAME, QueueClientType.PRODUCER);

    /* rabbit topology exit connection */
    private static Connection connectionExit;
    private static Channel channelExit;
    private static final String RABBIT_EXIT_HOST = "localhost";
    private static final Integer RABBIT_EXIT_PORT = 5673;
    private static final String  EXIT_EXCHANGE_NAME = "dashboard_exchange";
    private static String EXIT_QUEUE_NAME ;
//    RabbitPubSubManager rabbitPubSubManagerExit =
//            new RabbitPubSubManager(RABBIT_EXIT_HOST, RABBIT_EXIT_PORT, EXIT_EXCHANGE_NAME, QueueClientType.CONSUMER);


    private static final String[] routingKeys = {"dashboard.anomalies", "dashboard.rank" , "dashboard.statistics.lamps","dashboard.statistics.streets",
                                                    "dashboard.statistics.global"};
    /* topic based pub/sub */
    private  static final String EXIT_EXCHANGE_TYPE = "topic";
    private static final String EXIT_ROUTING_KEY_BASE = "dashboard."; //to be completed for different topologies
    private static Consumer exitConsumer;



    private static Random random;
    private static float FAILURE_PROB = .3f;
    private static Float GAUSSIAN_MEAN = 60f;
    private static Float GAUSSIAN_STDEV = 15f;

    /* json converter */
    private static Gson gson;

    private static String topologyName = "NONE";

    private static String testNumber = "test1";
    private static volatile boolean doingTest = false;



    public static void main(String[] args){

        random = new Random(12345);
        gson = new Gson();

        connectToRabbits();

        if (args.length != 2) {
            HeliosLog.logToScreen(LOG_TAG, "Usage: java -jar TopologyTest <topologyName> test<test number>");
            HeliosLog.logToScreen("", "topologyName = anomaly, statistics, ranking");
            System.exit(1);
        }

        topologyName = args[0];
        testNumber = args[1];

        switch (topologyName){
            case "anomaly":
                HeliosLog.logToScreen(LOG_TAG,"Starting Anomaly Topology Testing");
                switch (testNumber){
                    case "test1":
                        anomalyTest1();
                        break;
                    case "test2":
                        anomalyTest2();
                        break;
                    default:
                        HeliosLog.logFail(LOG_TAG,"No Such Test. Try again with test1, test2 ...");
                        System.exit(1);
                }
                break;
            case "statistics":
                HeliosLog.logToScreen(LOG_TAG,"Starting Statistics Topology Testing");

                break;

            case "ranking":
                HeliosLog.logToScreen(LOG_TAG,"Starting Ranking Topology Testing");
                switch (testNumber){
                    case "test1":
                        rankingTest1();
                        break;
                    default:
                        HeliosLog.logFail(LOG_TAG,"No Such Test. Try again with test1, test2 ...");
                        System.exit(1);
                }
        }
    }

    /**
     * tests whether a message sent to Anomaly Detection Topology
     * returns after a while with unchanged lamp values (ID,light level,address,etc)
     * but also that has a NOT_RESPONDING anomaly since it sent only a message and no other
     * after. it also tests if a message anomaly is redirected on the correct
     * topic (dashboard.anomalies) on the final rabbit queue
     */
    private static void anomalyTest1() {
        beginTest();
        HeliosLog.logToScreen(LOG_TAG,"Starting  " + TermCol.ANSI_BLUE + "Anomaly Topology Test 1" + TermCol.ANSI_RESET);


        StreetLampMessage lampMessage = generateRandomStreetLight();
        String json = gson.toJson(lampMessage);
        HeliosLog.logToScreen(LOG_TAG,json);

        Consumer consumer = new DefaultConsumer(channelExit) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                HeliosLog.logToScreen(LOG_TAG,"Received : " + message);

                if(envelope.getRoutingKey().equals("dashboard.anomalies")){

                    AnomalyStreetLampMessage anomalyMess = gson.fromJson(message,AnomalyStreetLampMessage.class);

                    boolean correctID;
                    if (anomalyMess.getStreetLamp().getID() == lampMessage.getStreetLamp().getID()) correctID = true;
                    else correctID = false;

                    boolean correctAnomaly = anomalyMess.getAnomalies().get(MalfunctionType.NOT_RESPONDING) != null;
                    boolean unchangedLightLevel = Objects.equals(anomalyMess.getNaturalLightLevel(), lampMessage.getNaturalLightLevel());
                    boolean unchangedAddress =  lampMessage.getStreetLamp().getAddress().getName().equals(anomalyMess.getStreetLamp().getAddress().getName());

                    if(correctID && correctAnomaly && unchangedLightLevel && unchangedAddress) HeliosLog.logOK(LOG_TAG,"Anomaly Test 1 OK");
                    else HeliosLog.logFail(LOG_TAG,"Anomaly Test 1 FAILED");

                    endTest();
                }
            }
        };


        try {
            channelExit.basicConsume(EXIT_QUEUE_NAME, false, consumer);
            channelEntry.basicPublish("", "storm", null, json.getBytes());
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * sends 4 different lamp messages, 3 with same
     * lamp state (ON/OFF) and waits for result on the end queue;
     * tests if the mess with ID the one with different state
     * has BULB_DAMAGE anomaly
     */
    private static void anomalyTest2() {
        waitForTest();
        beginTest();
        HeliosLog.logToScreen(LOG_TAG,"Starting  " + TermCol.ANSI_BLUE + "Anomaly Topology Test 2" + TermCol.ANSI_RESET);

        StreetLampMessage lamp1 = generateRandomStreetLight();
        StreetLampMessage lamp2 = generateRandomStreetLight();
        lamp2.getStreetLamp().setOn(lamp1.getStreetLamp().isOn());

        StreetLampMessage lamp3 = generateRandomStreetLight();
        lamp3.getStreetLamp().setOn(lamp1.getStreetLamp().isOn());

        StreetLampMessage lamp4 = generateRandomStreetLight();
        lamp4.getStreetLamp().setOn(!lamp1.getStreetLamp().isOn());


        Consumer consumer = new DefaultConsumer(channelExit){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                AnomalyStreetLampMessage anomalyMess = gson.fromJson(message,AnomalyStreetLampMessage.class);
                if(envelope.getRoutingKey().equals("dashboard.anomalies")){
                    HeliosLog.logToScreen(LOG_TAG,"Received " + message);
                }

                if(anomalyMess.getStreetLamp().getID() == lamp4.getStreetLamp().getID()){
                    if(anomalyMess.getAnomalies().get(MalfunctionType.DAMAGED_BULB) != null){
                        HeliosLog.logOK(LOG_TAG,"Anomaly Test 2 OK");
                    } else {
                        HeliosLog.logFail(LOG_TAG,"Anomaly Test 2 FAILED");
                    }
                }

                endTest();
            }
        };

        try {
            channelExit.basicConsume(EXIT_QUEUE_NAME, false, consumer);
            channelEntry.basicPublish("", "storm", null, gson.toJson(lamp1).getBytes());
            channelEntry.basicPublish("", "storm", null, gson.toJson(lamp2).getBytes());
            channelEntry.basicPublish("", "storm", null, gson.toJson(lamp3).getBytes());
            channelEntry.basicPublish("", "storm", null, gson.toJson(lamp4).getBytes());
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void rankingTest1() {
        waitForTest();
        beginTest();
        HeliosLog.logToScreen(LOG_TAG,"Starting  " + TermCol.ANSI_BLUE + "Ranking Oldest K Lamps Topology Test 1" + TermCol.ANSI_RESET);

        StreetLampMessage lamp1 = generateRandomStreetLight();
        lamp1.getStreetLamp().setLifetime(LocalDateTime.now().minusDays(5));

        StreetLampMessage lamp1_old = lamp1;
        lamp1_old.setTimestamp(lamp1.getTimestamp()-50);

        StreetLampMessage lamp2 = generateRandomStreetLight();
        lamp2.getStreetLamp().setLifetime(LocalDateTime.now().minusDays(16));

        StreetLampMessage lamp3 = generateRandomStreetLight();
        lamp3.getStreetLamp().setLifetime(LocalDateTime.now().minusDays(15));

        StreetLampMessage lamp4 = generateRandomStreetLight();
        lamp4.getStreetLamp().setLifetime(LocalDateTime.now().minusDays(14));

        StreetLampMessage lamp5 = generateRandomStreetLight();
        lamp5.getStreetLamp().setLifetime(LocalDateTime.now().minusDays(13));

        Integer expected_count = 4;
        List<RankLamp> expected_ranking = new ArrayList<> (3);
        expected_ranking.add(new RankLamp(
                lamp2.getStreetLamp().getID(),
                lamp2.getStreetLamp().getAddress(),
                lamp2.getStreetLamp().getLifetime(),
                lamp2.getTimestamp()));
        expected_ranking.add(new RankLamp(
                lamp3.getStreetLamp().getID(),
                lamp3.getStreetLamp().getAddress(),
                lamp3.getStreetLamp().getLifetime(),
                lamp3.getTimestamp()));
        expected_ranking.add(new RankLamp(
                lamp4.getStreetLamp().getID(),
                lamp4.getStreetLamp().getAddress(),
                lamp4.getStreetLamp().getLifetime(),
                lamp4.getTimestamp()));

        Consumer consumer = new DefaultConsumer(channelExit){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                RankingResults rankingResults = JSONConverter.toRankingResults(message);
                if(envelope.getRoutingKey().equals("dashboard.rank")){
                    HeliosLog.logToScreen(LOG_TAG,"Received " + message);
                }

                List<RankLamp> ranking = rankingResults.getRanking();

                if (rankingResults.getCount() == expected_count
                        && !(expected_ranking.stream().filter(r -> {
                    Integer index = expected_ranking.indexOf(r);
                    return r.getId() != ranking.get(index).getId();
                }).count() > 0)) {

                    HeliosLog.logOK(LOG_TAG,"Ranking Oldest 3 Lamp Replacements Test 1 OK");

                } else

                    HeliosLog.logFail(LOG_TAG,"Ranking Oldest 3 Lamp Replacements Test 1 FAILED");

                endTest();
            }
        };

        try {
            channelExit.basicConsume(EXIT_QUEUE_NAME, false, consumer);
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp1).getBytes());
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp2).getBytes());
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp3).getBytes());
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp4).getBytes());
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp5).getBytes());
            channelEntry.basicPublish("", "storm", null, JSONConverter.fromStreetLampMessage(lamp1_old).getBytes());
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void waitForTest() {
        while (doingTest){
            HeliosLog.logToScreen(LOG_TAG,"WAITING");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * exit after every test
     */
    private static void endTest() {
        doingTest = false;
        HeliosLog.logToScreen(LOG_TAG,"END");
        System.exit(0);
    }

    /* at the beginning of every test */
    private static void beginTest() {
        doingTest = true;
    }

    /**
     * connect to first queue and last on the topology
     */
    private static void connectToRabbits() {
        /* entry */
        ConnectionFactory factoryEntry = new ConnectionFactory();
        factoryEntry.setHost(RABBIT_ENTRY_HOST);
        factoryEntry.setPort(RABBIT_ENTRY_PORT);

        /* exit */
        ConnectionFactory factoryExit = new ConnectionFactory();
        factoryExit.setHost(RABBIT_EXIT_HOST);
        factoryExit.setPort(RABBIT_EXIT_PORT);

        try {
            connectionEntry = factoryEntry.newConnection();
            channelEntry = connectionEntry.createChannel();
            channelEntry.queueDeclare(RABBIT_ENTRY_QUEUE_NAME,false,false,false,null);


            connectionExit = factoryExit.newConnection();
            channelExit = connectionExit.createChannel();
            channelExit.exchangeDeclare(EXIT_EXCHANGE_NAME,EXIT_EXCHANGE_TYPE);

            EXIT_QUEUE_NAME = channelExit.queueDeclare().getQueue();

            for(String k : routingKeys){
                channelExit.queueBind(EXIT_QUEUE_NAME,EXIT_EXCHANGE_NAME,k);
            }


        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            HeliosLog.logToScreen(LOG_TAG,"Rabbit Connection Failed");
        }
    }

    private static StreetLampMessage generateRandomStreetLight() {
        Address address = new Address();
        address.setName("Via Politecnico");
        address.setNumber(11);
        /*
        address.setName(randomStreetName());
        address.setNumber(generateRandomInt());
        */
        address.setNumberType(AddressNumberType.CIVIC);

        StreetLamp streetLamp = new StreetLamp();
        streetLamp.setAddress(address);

        // sometimes generate lamp in a specific cell park, other times lamp externally a cell park
        int i = generateRandomInt();
        if ( i % 2 == 0) {
            streetLamp.setCellID(i);
        } else {
            streetLamp.setCellID(-1);
        }

        streetLamp.setID(generateRandomInt());
        streetLamp.setLightIntensity(generateRandomFloatGaussian());

        streetLamp.setLampModel(Lamp.LED);
        streetLamp.setCellID(generateRandomInt());
        streetLamp.setOn(randomMalfunctioning());
        streetLamp.setConsumption(generateRandomFloat());
        streetLamp.setLifetime(LocalDateTime.now().minus(generateRandomInt() % 100, ChronoUnit.DAYS));

        StreetLampMessage message = new StreetLampMessage();
        message.setNaturalLightLevel(generateRandomFloat());
        message.setStreetLamp(streetLamp);
        message.setTimestamp(System.currentTimeMillis() - (long) (Math.random() * 1000000));

        return message;
    }

    /**
     * Normal(GAUSSIAN_MEAN,GAUSSIAN_STDEV)
     * @return random float from Normal dist with
     * mean GAUSSIAN_MEAN and stdev GAUSSIAN_STDEV
     */
    private static float generateRandomFloatGaussian() {

        return (float) random.nextGaussian() * GAUSSIAN_STDEV + GAUSSIAN_MEAN;
    }

    private static String randomStreetName() {

        float rand = (float) Math.random();

        if(rand < 0.5){
            return "VIA del POLITECNICO";
        } else {
            return "VIA CAMBRIDGE";
        }
    }

    private static boolean randomMalfunctioning() {
        float rand = (float) Math.random();
        if(rand < FAILURE_PROB){
            return false;
        }

        return true;
    }

    private static float generateRandomFloat() {

        float rand =(float) (Math.random() * 100);
        return rand;
    }

    private static int generateRandomInt() {

        int rand = (int) (Math.random() * 100000);
        return rand;
    }

    private class TermCol{
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
    }
}
