package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Model.StreetLamp;
import org.uniroma2.sdcc.Model.StreetLampMessage;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ovidiudanielbarba on 21/03/2017.
 */
public class MalfunctionCheckBolt implements IRichBolt {

    private OutputCollector outputCollector;

    private float receivedMessages = 0f;
    private float malfunctioningLamps = 0f;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.outputCollector = collector;

        printTimerStart();
    }


    @Override
    public void execute(Tuple input) {

        StreetLampMessage message = (StreetLampMessage) input.getValueByField(StreetLampMessage.STREET_LAMP_MSG);

        incrementReceivedMessages();
        countMalfunctioning(message);

        outputCollector.ack(input);
    }

    private void incrementReceivedMessages() {
        /* avoid overflow */
        if( receivedMessages == Long.MAX_VALUE){
            receivedMessages = 0f;
            malfunctioningLamps = 0f;
        }

        receivedMessages++;
    }

    /**
     * counts malfunctioning street lamps
     * @param message
     */
    private void countMalfunctioning(StreetLampMessage message) {

        StreetLamp lamp = message.getStreetLamp();
        boolean on = lamp.isOn();

        if(!on){
            malfunctioningLamps++;
        }
    }

    /**
     * print every 30 seconds percentage of malfunctioning street lamps
     */
    private void printTimerStart() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(receivedMessages != 0) {
                    System.out.println("[CINI] " + String.format("%.2f", malfunctioningLamps / receivedMessages * 100) + "% MALFUNCTIONING STREET LAMPS. \n");
                }
            }
        }, 60000,10000);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final Bolt; nothing to declare
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
