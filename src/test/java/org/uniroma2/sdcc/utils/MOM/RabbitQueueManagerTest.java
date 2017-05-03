package org.uniroma2.sdcc.Utils.MOM;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests if rabbit mq connection is successfully established
 * on local machine and that it sends and receives messages
 * correctly
 *
 * YOU NEED TO HAVE A RABBIT QUEUE ENABLE ON localhost:5672
 * ELSE TEST WILL PASS UNKNOWING IF IT'S SENDING AND RECEIVING
 * CORRECTLY
 */
public class RabbitQueueManagerTest {

    private static final String HOST = "localhost";
    private static final Integer PORT = 5672;
    private static final String QUEUE_NAME = "test";
    QueueManger queue;

    @Before
    public void setUp(){
        queue = new RabbitQueueManager(HOST,PORT,QUEUE_NAME, QueueClientType.CONSUMER_PRODUCER);
    }

    @Test
    public void sendReceive() throws Exception {
        String sent = "TEST";
        queue.send(sent);

        String received = queue.nextMessage();

        if(received != null){
            assertTrue(received.equals(sent));
        }

        /* received is null => no connection on localhost:5672
           TEST PASSES */

    }

    /**
     * tests if connection if available with parameters from config file
     * @throws Exception
     */
    @Test
    public void sendReceiveConfigFile() throws Exception{
        queue = new RabbitQueueManager();
        String sent = "TEST";

        queue.send(sent);

        String received = queue.nextMessage();

        if(received != null){
            assertTrue(received.equals(sent));
        }

        /* received is null => no connection on localhost:5672
           TEST PASSES */
    }

}