package org.uniroma2.sdcc.Utils.MOM;

import clojure.lang.IFn;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.uniroma2.sdcc.Utils.Config.RabbitConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;
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
    protected static final String QUEUE_IN = "queueIn";
    protected static final String QUEUE_OUT = "queueOut";

    private RabbitConfig rabbitConfig;

    /* rabbitmq connection variables */
    protected Connection connection;
    protected Channel channel;
    protected ConnectionFactory factory;
    protected String host;
    protected Integer port;
    protected String queueName;

    /* connection available */
    protected boolean available;

    protected QueueClientType type;

    public AbstractRabbitMQManager(String host, Integer port,String queue,QueueClientType type){
        this.host = host;
        this.port = port;
        this.queueName = queue;
        this.type = type;

        setUpFactory();
        tryConnection();
    }

    /**
     * set factory parameters
     */
    private void setUpFactory() {
        factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
    }


    /**
     * contructor with only queue type as parameter
     * which sets values taken from configuration file if present
     * else sets default values
     * @param queueType in(from spput) or out (to dashboard)
     */
    public AbstractRabbitMQManager(String queueType) {
        configFromFile(queueType);
    }

    /**
     * take config values from file (if present)
     * @param queueType in or out
     */
    private void configFromFile(String queueType) {
        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();
        try {
            switch (queueType) {
                case QUEUE_IN:
                    rabbitConfig = yamlConfigRunner.getConfiguration().getQueue_in();
                    this.type = QueueClientType.CONSUMER_PRODUCER;
                    break;
                case QUEUE_OUT:
                    rabbitConfig = yamlConfigRunner.getConfiguration().getQueue_out();
                    this.type = QueueClientType.PRODUCER;
                    break;
            }

            /* set from config file */
            this.host = rabbitConfig.getHostname();
            this.port = rabbitConfig.getPort();
            this.queueName = rabbitConfig.getQueue_name();

        } catch (IOException e){
            e.printStackTrace();
            HeliosLog.logFail(LOG_TAG,"File config error. Using default values");

            /* default values */
            this.host = "localhost";
            switch (queueType){
                case QUEUE_IN:
                    this.port = 5672;
                    this.queueName = "storm";
                    this.type = QueueClientType.CONSUMER;
                    break;
                case QUEUE_OUT:
                    this.port = 5672;
                    this.queueName = "dashboard_exchange";
                    this.type = QueueClientType.PRODUCER;
                    break;
            }
        } finally {
            setUpFactory();
            tryConnection();
        }
    }

    /* try to connect to rabbitmq
         * else sets connection and channel to null
         * can be recalled later to retry connection
         */
    protected void tryConnection(){

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            setAvailable(true);

        } catch (IOException | TimeoutException e) {

            e.printStackTrace();
            /* set connection to null */
            connection = null;
            channel = null;
            /* make unavailable*/
            setAvailable(false);
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


    protected void setAvailable(Boolean available){
        this.available = available;
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
