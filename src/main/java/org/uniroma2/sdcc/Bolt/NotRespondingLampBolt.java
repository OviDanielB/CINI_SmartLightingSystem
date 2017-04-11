package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by ovidiudanielbarba on 30/03/2017.
 */
public class NotRespondingLampBolt implements IRichBolt {

    private OutputCollector collector;
    /* (K,V) -> (LampID, Timestamp last received message) */
    private ConcurrentHashMap<Integer, AnomalyStreetLampMessage> notRespondingCount;

    private ConcurrentLinkedQueue<AnomalyStreetLampMessage> noResponseLampsToRabbit;

    /* in seconds */
    private static final Integer RESPONSE_CHECKER_PERIOD = 10;

    /* time after which a lamp not responding is considered malfunctioning  */
    private static final Long NO_RESPONSE_INTERVAL = 60L;

    private static final String LOG_TAG = "[CINI] [NotRespondingLampBolt] ";


    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        /* initialization */
        notRespondingCount = new ConcurrentHashMap<>();
        noResponseLampsToRabbit = new ConcurrentLinkedQueue<>();

        startPeriodicResponseChecker();
    }

    @Override
    public void execute(Tuple input) {

        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<MalfunctionCheckBolt, Float>>(){}.getType();
        HashMap<MalfunctionType,Float> malfunctions =
                (HashMap<MalfunctionType, Float>) input.getValueByField(StreetLampMessage.MALFUNCTIONS_TYPE);
               /* gson.fromJson(input.getValueByField(StreetLampMessage.MALFUNCTIONS_TYPE).toString(), type); */

        Integer id = (Integer) input.getValueByField(StreetLampMessage.ID);
        Address address = (Address) input.getValueByField(StreetLampMessage.ADDRESS);
        Boolean on = (Boolean) input.getValueByField(StreetLampMessage.ON);
        String model = (String) input.getValueByField(StreetLampMessage.LAMP_MODEL);
        Float consumption = (Float) input.getValueByField(StreetLampMessage.CONSUMPTION);
        LocalDateTime lifeTime = (LocalDateTime) input.getValueByField(StreetLampMessage.LIFETIME);
        Float intensity = (Float) input.getValueByField(StreetLampMessage.INTENSITY);
        Float naturalLightLevel = (Float) input.getValueByField(StreetLampMessage.NATURAL_LIGHT_LEVEL);
        Long timestamp = (Long) input.getValueByField(StreetLampMessage.TIMESTAMP);

        StreetLamp lamp = new StreetLamp(id,on,getLampModelByString(model),
                address,intensity,consumption,lifeTime);

        //HashMap<MalfunctionType, Float> malfunctions = parseStringForMalf(malfunctionsStr);

        AnomalyStreetLampMessage anomalyMessage = new AnomalyStreetLampMessage(lamp,naturalLightLevel,
                timestamp, malfunctions,0L);


        updateLampList(id,anomalyMessage);


        collector.ack(input);

    }

    /**
     * parse string for lamp model
     * @param model string containing model
     * @return Lamp Model or Unknown if none present in the string
     */
    private Lamp getLampModelByString(String model) {

        for(Lamp lamp : Lamp.values()){
            if(model.contains(lamp.toString())){
                return lamp;
            }
        }

        return Lamp.UNKNOWN;
    }

    /**
     * periodically parses notRespondingCount hash map to determine those street lamps that haven't
     * sent messages for NO_RESPONSE_INTERVAL
     */
    private void startPeriodicResponseChecker() {

        /* start periodic producer on queue */
        Timer timer = new Timer();
        QueueProducer producer = new QueueProducer(noResponseLampsToRabbit,notRespondingCount);
        timer.schedule(producer,5000, 1000 * RESPONSE_CHECKER_PERIOD);


        /* start consumer thread on queue */
        Timer consumerTimer = new Timer();
        QueueConsumerToRabbit consumerToRabbit = new QueueConsumerToRabbit(noResponseLampsToRabbit);
        consumerTimer.schedule(consumerToRabbit,6000, 1000 * RESPONSE_CHECKER_PERIOD);
        //consumer = new Thread(consumerToRabbit);
        //consumer.start();


    }

    /* update lamp list with recent values */
    private void updateLampList(Integer id, AnomalyStreetLampMessage anomalyMess) {

        // TODO
        notRespondingCount.put(id, anomalyMess);
        //System.out.println(LOG_TAG + "HASH MAP LENGTH = " + notRespondingCount.size());

    }


    /**
     * parse string provided by MalfunctionCheckBolt and
     * retrieve MalfunctionTypes
     * @param malfunctionsStr string (may contain only NONE)
     * @return list of mapping between malfunctions and difference
     *          from correct value
     */
    private HashMap<MalfunctionType, Float> parseStringForMalf(String malfunctionsStr) {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        for(MalfunctionType t : MalfunctionType.values()){
            if(malfunctionsStr.contains(t.toString())){
                anomalies.put(t, 0f);
            } else {
                anomalies.put(t, 0f);
            }
        }

        return anomalies;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(AnomalyStreetLampMessage.STREET_LAMP_MSG));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }



    /**
     * Thread that executes periodically (given by TimerTask),
     * iterates the hashmap and produces in a queue the street lamps
     * that have not responded for a while
     */
    private class QueueProducer extends TimerTask{

        private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;

        private ConcurrentHashMap<Integer,AnomalyStreetLampMessage> hashMap;

        public QueueProducer(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue,
                             ConcurrentHashMap map) {
            this.queue = queue;
            this.hashMap = map;
        }

        @Override
        public void run() {

            hashMap.entrySet().stream()
                    .filter( e -> {
                        Long now = System.currentTimeMillis();
                        Long lastMessTime = e.getValue().getTimestamp();
                            /* if difference from time now and last received message
                             is greater than NO_RESPONSE_INTERVAL => it's not working anymore */
                        return ( now - lastMessTime ) > NO_RESPONSE_INTERVAL * 1000; /* in milliseconds */
                    }).forEach(e -> {

                        /* add another anomaly since it is not responding for a while */
                        adjustAnomaliesList(e);

                        queue.add(e.getValue());
                        //System.out.println(LOG_TAG + " QUEUE LENGTH =  " + queue.size());
                        hashMap.remove(e.getKey());
                        System.out.println("[CINI] Not Responding LAMP with ID " + e.getKey() + " has not responded for longer than " +
                               "" + NO_RESPONSE_INTERVAL + " seconds");

            });
        }

        private void adjustAnomaliesList(Map.Entry<Integer, AnomalyStreetLampMessage> e) {

            HashMap<MalfunctionType, Float> map = e.getValue().getAnomalies();

            if(map.containsKey(MalfunctionType.NONE)){
                map.remove(MalfunctionType.NONE);
            }

            map.put(MalfunctionType.NOT_RESPONDING,0f);
        }
    }

    /**
     * consumes messages from the queue and sends it on a rabbitmq
     */
    private class QueueConsumerToRabbit extends TimerTask{

        private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;
        private Connection connection;
        private Channel channel;
        private ConnectionFactory factory;

        private Gson gson;

        private  final String HOST = "localhost";
        private  final Integer PORT =  5673;
        private  final String QUEUE_NAME = "anomaly";
        private  final String  EXCHANGE_NAME = "dashboard_exchange";
        /* topic based pub/sub */
        private  final String EXCHANGE_TYPE = "topic";
        private  final String ROUTING_KEY = "dashboard.anomalies";

        public QueueConsumerToRabbit(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue) {
            System.out.println(LOG_TAG + "RABBIT CONSUMER CONSTRUCTOR");
            this.queue = queue;
            gson = new Gson();
            rabbitConnection();
        }

        /* set connection attributes */
        private void rabbitConnection() {
            factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);

            tryConnection();
        }

        /* try to connect to rabbitmq
         * else sets connection and channel to null
         * can be recalled later to retry connection
         */
        private void tryConnection(){

            try {
                connection = factory.newConnection();
                channel = connection.createChannel();

                //channel.queueDeclare(QUEUE_NAME,false,false,false,null);
                //System.out.println(LOG_TAG + " Rabbit connected on " + HOST+ ":" + PORT);

                /* declare exchange point, consumer must bind a queue to it */
                channel.exchangeDeclare(EXCHANGE_NAME,EXCHANGE_TYPE);

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                System.out.println(LOG_TAG + "No rabbit connection available! ");
                connection = null;
                channel = null;
            }

        }


        public boolean rabbitConnectionAvailable(){
            return ((connection != null) && (channel != null));
        }

        @Override
        public void run() {

            /* poll queue for new messages */
            AnomalyStreetLampMessage message = null;
            String jsonMsg;

            while( (message = queue.poll()) != null){

                /* convert to json */
                jsonMsg = gson.toJson(message);
                System.out.println(LOG_TAG + " sending " + jsonMsg);
                try {

                    if(rabbitConnectionAvailable()) {
                        /* write on queue */
                        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, jsonMsg.getBytes());
                    } else {
                        /* retry to connect */
                        tryConnection();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(LOG_TAG + "Could not publish message on final rabbit queue! ");
                }
            }
        }
    }

}
