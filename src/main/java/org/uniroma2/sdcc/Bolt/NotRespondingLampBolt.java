package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ovidiudanielbarba on 30/03/2017.
 */
public class NotRespondingLampBolt implements IRichBolt {

    private OutputCollector collector;
    /* (K,V) -> (LampID, Timestamp last received message) */
    private ConcurrentHashMap<Integer, Long> notRespondingCount;

    /* in seconds */
    private static final Integer RESPONSE_CHECKER_PERIOD = 10;

    /* time after which a lamp not responding is considered malfunctioning  */
    private static final Long NO_RESPONSE_INTERVAL = 60L;


    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        notRespondingCount = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(Tuple input) {

        String malfunctionsStr = (String) input.getValueByField(StreetLampMessage.MALFUNCTIONS_TYPE);
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

        List<MalfunctionType> malfunctions = parseStringForMalf(malfunctionsStr);

        AnomalyStreetLampMessage anomalyMessage = new AnomalyStreetLampMessage(lamp,naturalLightLevel,
                timestamp, malfunctions,0);

        // TODO continue
        updateNotRespCount(id,timestamp);

        startPeriodicResponseChecker();

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

    private void startPeriodicResponseChecker() {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                notRespondingCount.entrySet().stream()
                        .filter( e -> {
                            Long now = System.currentTimeMillis();
                            Long lastMessTime = e.getValue();
                            /* if difference from time now and last received message
                             is greater than NO_RESPONSE_INTERVAL => it's not working anymore */
                            return ( now - lastMessTime ) > NO_RESPONSE_INTERVAL;
                        }).mapToInt(e -> e.getKey());

            }
        },5000, 1000 * RESPONSE_CHECKER_PERIOD);


    }

    private void updateNotRespCount(Integer id, Long timestamp) {
        notRespondingCount.putIfAbsent(id, timestamp);

        Long count = notRespondingCount.get(id);
        count++;
    }


    /**
     * parse string provided by MalfunctionCheckBolt and
     * retrieve MalfunctionTypes
     * @param malfunctionsStr string (may contain only NONE)
     * @return list of malfunctions
     */
    private List<MalfunctionType> parseStringForMalf(String malfunctionsStr) {

        List<MalfunctionType> malfunctionTypes = new ArrayList<>();
        for(MalfunctionType t : MalfunctionType.values()){
            if(malfunctionsStr.contains(t.toString())){
                malfunctionTypes.add(t);
            }
        }

        return malfunctionTypes;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
