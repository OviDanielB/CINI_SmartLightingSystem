package org.uniroma2.sdcc.Utils.MOM;

import org.uniroma2.sdcc.Utils.HeliosLog;

import java.io.IOException;

/**
 * Class for RabbitMQ pub/sub mode managements
 */
public class RabbitPubSubManager extends AbstractRabbitMQManager implements PubSubManager {

    private final String EXCHANGE_TYPE = "topic";

    public RabbitPubSubManager(String host, Integer port,String exchangeName, QueueClientType type) {
        super(host, port,exchangeName, type);

        declareExchange();
    }

    /**
     * constructor using only exchangeName
     * and takes values from config file
     */
    public RabbitPubSubManager() {
        super(QUEUE_OUT);

        declareExchange();
    }

    /**
     * declare exchange for pub/sub
     */
    private void declareExchange() {

        /* declare exchange point, consumer must bind a queue to it */
        try {
            channel.exchangeDeclare(queueName,EXCHANGE_TYPE);

        } catch (IOException e) {
            e.printStackTrace();
            HeliosLog.logFail(LOG_TAG,"Exchange declaration failed");
            setAvailable(false);
        }
    }

    @Override
    public boolean publish(String routingKey, String message) {
        if(isAvailable() && isProducer() && message != null){
            try {
                channel.basicPublish(queueName,routingKey,null, message.getBytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                HeliosLog.logFail(LOG_TAG,"Couldn't send message on exchange");
                return false;
            }
        } else {
            HeliosLog.logFail(LOG_TAG,"Can't publish. Component is not a producer or connection is not available");
        }
        return false;
    }
}
