package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Config.RabbitConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;
import org.uniroma2.sdcc.Utils.HeliosLog;
import org.uniroma2.sdcc.Utils.JSONConverter;
import org.uniroma2.sdcc.Utils.MOM.PubSubManager;
import org.uniroma2.sdcc.Utils.MOM.QueueClientType;
import org.uniroma2.sdcc.Utils.MOM.RabbitPubSubManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private static final Long NO_RESPONSE_INTERVAL = 60L; /* seconds */

    private static final String LOG_TAG = "[NotRespondingLampBolt]";


    /**
     * Bolt initialization
     *
     * @param stormConf conf
     * @param context context
     * @param collector collector
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        /* initialization */
        notRespondingCount = new ConcurrentHashMap<>();
        noResponseLampsToRabbit = new ConcurrentLinkedQueue<>();

        startPeriodicResponseChecker();
    }

    /**
     * Bolt operation on incoming tuple.
     *
     * @param input tuple received
     */
    @Override
    public void execute(Tuple input) {

        HashMap<MalfunctionType,Float> malfunctions =
                (HashMap<MalfunctionType, Float>) input.getValueByField(Constants.MALFUNCTIONS_TYPE);

        Integer id = (Integer) input.getValueByField(Constants.ID);
        Address address = (Address) input.getValueByField(Constants.ADDRESS);
        Integer cellID = (Integer) input.getValueByField(Constants.CELL);
        Boolean on = (Boolean) input.getValueByField(Constants.ON);
        String model = (String) input.getValueByField(Constants.LAMP_MODEL);
        Float consumption = (Float) input.getValueByField(Constants.CONSUMPTION);
        LocalDateTime lifeTime = (LocalDateTime) input.getValueByField(Constants.LIFETIME);
        Float intensity = (Float) input.getValueByField(Constants.INTENSITY);
        Float naturalLightLevel = (Float) input.getValueByField(Constants.NATURAL_LIGHT_LEVEL);
        Long timestamp = (Long) input.getValueByField(Constants.TIMESTAMP);

        StreetLamp lamp = new StreetLamp(id,on,getLampModelByString(model),
                address, cellID, intensity,consumption,lifeTime);

        /* construct anomaly message */
        AnomalyStreetLampMessage anomalyMessage = new AnomalyStreetLampMessage(lamp,naturalLightLevel,
                timestamp, malfunctions, 0L);

        updateLampList(id,anomalyMessage);


        Values values = new Values();
        values.add(malfunctions);
        values.add(id);
        values.add(address);
        values.add(cellID);
        values.add(on);
        values.add(model);
        values.add(consumption);
        values.add(lifeTime);
        values.add(intensity);
        values.add(naturalLightLevel);
        values.add(timestamp);

        collector.emit(input,values);

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
     * sent messages for NO_RESPONSE_INTERVAL time
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


    }

    /* update lamp list with recent values */
    private void updateLampList(Integer id, AnomalyStreetLampMessage anomalyMess) {

        // TODO
        notRespondingCount.put(id, anomalyMess);

    }

    @Override
    public void cleanup() {

    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                Constants.MALFUNCTIONS_TYPE,
                Constants.ID,
                Constants.ADDRESS,
                Constants.CELL,
                Constants.ON,
                Constants.LAMP_MODEL,
                Constants.CONSUMPTION,
                Constants.LIFETIME,
                Constants.INTENSITY,
                Constants.NATURAL_LIGHT_LEVEL,
                Constants.TIMESTAMP));
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
        /* queue where it produces */
        private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;

        /* hashmap from where it gets anomaly messages */
        private ConcurrentHashMap<Integer,AnomalyStreetLampMessage> hashMap;

        /* constructor */
        public QueueProducer(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue,
                             ConcurrentHashMap map) {
            this.queue = queue;
            this.hashMap = map;
        }

        /**
         * iterates hashmap, checks if messages
         * have not been updated for more than
         * NO_RESPONSE_INTERVAL time (meaning that the lamp
         * is not sending messages anymore) and puts the old ones
         * on a linked queue (where a consumer awaits to send them)
         */
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

                        /* adds on final queue, where consumer is present */
                        queue.add(e.getValue());

                        /* removes from hashMap (avoid being processed multiple times )*/
                        hashMap.remove(e.getKey());
                        System.out.println("[CINI] Not Responding LAMP with ID " + e.getKey() + " has not responded for longer than " +
                               "" + NO_RESPONSE_INTERVAL + " seconds");

            });
        }

        /**
         * adds NOT_RESPONDING anomaly
         * @param e (K, V) -> (Lamp ID, message received)
         */
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

        /* queue where it consumes*/
        private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;

        /* message -> json -> rabbit*/
        private PubSubManager pubSubManager;

        /* topic based pub/sub */
        private  final String ROUTING_KEY = "dashboard.anomalies";

        /* constructor */
        public QueueConsumerToRabbit(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue) {
            this.queue = queue;

            /* connect to rabbit with pub/sub mode */
            pubSubManager = new RabbitPubSubManager();

        }

        /**
         * continuously polls queue for new messages;
         * if message present, takes it ,
         * converts to json and sends on queue
         */
        @Override
        public void run() {

            /* poll queue for new messages */
            AnomalyStreetLampMessage message;
            String jsonMsg;

            while( (message = queue.poll()) != null){

                /* convert to json */
                jsonMsg = JSONConverter.fromAnomalyStreetLampMessage(message);

                /* publish with relative routing key */
                pubSubManager.publish(ROUTING_KEY,jsonMsg);
            }
        }
    }

}
