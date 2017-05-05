package org.uniroma2.sdcc.Utils.MOM;

import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Model.MalfunctionType;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread that executes periodically (given by TimerTask),
 * iterates the hashmap and produces in a queue the street lamps
 * that have not responded for a while
 */
public class AnomalyQueueProducer extends TimerTask {
    /* queue where it produces */
    private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;

    /* hashmap from where it gets anomaly messages */
    private ConcurrentHashMap<Integer,AnomalyStreetLampMessage> hashMap;

    /* time after which a lamp not responding is considered malfunctioning  */
    private static final Long NO_RESPONSE_INTERVAL = 60L; /* seconds */

    /* constructor */
    public AnomalyQueueProducer(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue,
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
