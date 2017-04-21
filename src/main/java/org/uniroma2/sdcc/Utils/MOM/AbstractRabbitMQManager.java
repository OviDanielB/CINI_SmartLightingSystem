package org.uniroma2.sdcc.Utils.MOM;

import clojure.lang.IFn;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.uniroma2.sdcc.Utils.HeliosLog;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Defines basic RabbitMQ connection operations
 * like setting connection host and port,
 * try to connect and create channel,
 * check if connection is available
 * and finally close connection
 */
public abstract class AbstractRabbitMQManager {

    protected static final String LOG_TAG = "[RABBITMANAGER]";

    /* rabbitmq connection variables */
    protected Connection connection;
    protected Channel channel;
    protected ConnectionFactory factory;
    protected String host;
    protected Integer port;

    /* connection available */
    protected boolean available;

    protected QueueClientType type;

    public AbstractRabbitMQManager(String host, Integer port,QueueClientType type){
        this.host = host;
        this.port = port;

        this.type = type;

        factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);

        tryConnection();
    }

    /* try to connect to rabbitmq
     * else sets connection and channel to null
     * can be recalled later to retry connection
     */
    protected void tryConnection(){

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            available = true;

        } catch (IOException | TimeoutException e) {

            e.printStackTrace();
            /* set connection to null */
            connection = null;
            channel = null;
            /* make unavailable*/
            available = false;
            HeliosLog.logFail(LOG_TAG, "Connection Failed");
        }
    }

    /**
     * check if rabbit connection available
     * @return true if available, false otherwise
     */
    protected boolean isAvailable() {
        return available;
    }

    /**
     * close connection resources
     */
    public void closeConnection(){

        if(channel != null && connection != null) {
            try {
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                HeliosLog.logFail(LOG_TAG,"Can't close connection.");
            }

        }
    }


    /**
     * @return true if it's a consumer,false otherwise
     */
    protected boolean isConsumer(){
        return type.equals(QueueClientType.CONSUMER) || type.equals(QueueClientType.CONSUMER_PRODUCER);
    }

    /**
     * @return true if it's a producer, false otherwise
     */
    protected boolean isProducer(){
        return type.equals(QueueClientType.PRODUCER) || type.equals(QueueClientType.CONSUMER_PRODUCER);
    }

    /**
     * @return true if it's either a consumer or producer
     */
    protected boolean isConsumerProducer(){
        return type.equals(QueueClientType.CONSUMER_PRODUCER);
    }
}
