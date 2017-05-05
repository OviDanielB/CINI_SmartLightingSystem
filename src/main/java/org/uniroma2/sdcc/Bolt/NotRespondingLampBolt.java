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
import org.uniroma2.sdcc.Utils.MOM.AnomalyQueueConsumerToRabbit;
import org.uniroma2.sdcc.Utils.MOM.AnomalyQueueProducer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Bolt that saves last message (with relative timestamp)
 * received by every single lamp and detects if it hasn't
 * responded in a certain parametrized period
 */
public class NotRespondingLampBolt implements IRichBolt {

    private OutputCollector collector;

    /* (K,V) -> (LampID, Timestamp last received message) */
    private ConcurrentHashMap<Integer, AnomalyStreetLampMessage> notRespondingCount;
    private ConcurrentLinkedQueue<AnomalyStreetLampMessage> noResponseLampsToRabbit;

    /* in seconds */
    private static final Integer RESPONSE_CHECKER_PERIOD = 10;

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
        AnomalyQueueProducer producer = new AnomalyQueueProducer(noResponseLampsToRabbit,notRespondingCount);
        timer.schedule(producer,5000, 1000 * RESPONSE_CHECKER_PERIOD);


        /* start consumer thread on queue */
        Timer consumerTimer = new Timer();
        AnomalyQueueConsumerToRabbit consumerToRabbit = new AnomalyQueueConsumerToRabbit(noResponseLampsToRabbit);
        consumerTimer.schedule(consumerToRabbit,6000, 1000 * RESPONSE_CHECKER_PERIOD);
    }

    /* update lamp list with recent values */
    private void updateLampList(Integer id, AnomalyStreetLampMessage anomalyMess) {

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
}
