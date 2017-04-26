package org.uniroma2.sdcc.Utils.MOM;

import clojure.lang.IFn;
import com.rabbitmq.client.*;
import org.uniroma2.sdcc.Utils.HeliosLog;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class for RabbitMQ simple queue management with basic operations
 * (sending and receiving). Extends AbstractRabbitMQManager so it inherits connection
 * management methods and implements QueueManger defining sending and receiving
 * operations
 */
public class RabbitQueueManager extends AbstractRabbitMQManager implements QueueManger {

    private Consumer consumer;

    private ConcurrentLinkedQueue<String> messages;

    /**
     * constructor
     * @param host rabbitmq host
     * @param port rabbitmq port
     * @param queueName queue Name
     */
    public RabbitQueueManager(String host, Integer port,String queueName, QueueClientType type) {
        super(host, port,queueName, type);

        initialize();
    }

    public RabbitQueueManager() {
        super(QUEUE_IN);

        initialize();
    }


    private void initialize() {
        this.messages = new ConcurrentLinkedQueue<>();

        declareQueue(queueName);

        if(isConsumer() && isAvailable()) {
            consumer = createConsumer();
            startConsuming();
        }
    }

    /**
     * start consuming on channel
     * with previously created consumer
     */
    private void startConsuming() {
        try {
            channel.basicConsume(queueName,false,consumer);
        } catch (IOException e) {
            e.printStackTrace();
            HeliosLog.logFail(LOG_TAG,"Channel consuming FAILED");
        }
    }


    /**
     *  start a basic consumer on the created channel
     *  which inserts into a queue the received messages
     * @return a new Consumer, null if error
     */
    private Consumer createConsumer() {

        return new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");
                messages.add(message);

                /* ack received message */
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

    }

    /**
     * declare queue if connection available
     * @param queueName queue name
     */
    private void declareQueue(String queueName) {

        if(isAvailable()) {
            try {
                channel.queueDeclare(queueName, false, false, false, null);
            } catch (IOException e) {
                e.printStackTrace();
                HeliosLog.logFail(LOG_TAG, "Queue declaration Failed");
            }
        } else {
            HeliosLog.logFail(LOG_TAG, "No Rabbit connection Available");
        }
    }

    /**
     * send message if connection available
     * @param message message string
     * @return true if message sending succeded,
     *         false otherwise
     */
    @Override
    public boolean send(String message) {

        if(message == null){
            HeliosLog.logFail(LOG_TAG,"Trying to send null message");
            return false;
        }

        if(!isProducer()){
            HeliosLog.logFail(LOG_TAG,"Can't send. It's a consumer");
            return false;
        }
        if(isAvailable()){

            try {
                channel.basicPublish("",queueName,null,message.getBytes());
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                HeliosLog.logFail(LOG_TAG,"Message sending failed");
                return false;
            }

        } else {
            HeliosLog.logFail(LOG_TAG,"No connection available");
            return false;
        }

    }


    /**
     * BLOCKING METHOD
     * returns a message from the
     * queue and wait until one is present
     * @return message when available, null when no connection
     */
    @Override
    public String nextMessage() {

        if(!isAvailable()){
            HeliosLog.logFail(LOG_TAG,"No connection available. Can't receive any message");
            return null;
        }
        String mess;
        while(( mess = messages.poll()) == null){
            /* wait until at least one message available */
        }
        return mess;
    }

    @Override
    public void close() {
        super.closeConnection();
    }

}
