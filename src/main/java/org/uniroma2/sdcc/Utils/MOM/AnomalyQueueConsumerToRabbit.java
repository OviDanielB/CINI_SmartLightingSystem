package org.uniroma2.sdcc.Utils.MOM;

import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Utils.JSONConverter;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * consumes messages from the anomaly queue and sends it on a rabbitmq
 */
public class AnomalyQueueConsumerToRabbit extends TimerTask {
    /* queue where it consumes*/
    private ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue;

    /* message -> json -> rabbit*/
    private PubSubManager pubSubManager;

    /* topic based pub/sub */
    private  final String ROUTING_KEY = "dashboard.anomalies";

    /* constructor */
    public AnomalyQueueConsumerToRabbit(ConcurrentLinkedQueue<AnomalyStreetLampMessage> queue) {
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
