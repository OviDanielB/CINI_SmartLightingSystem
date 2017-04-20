package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Utils.HeliosLog;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.*;

/**
 * Test Suite for SNS Writer class
 */
public class SNSWriterTest {

    private ArrayBlockingQueue<String> queue;
    private ExecuteBolt.SNSWriter writer;


    @Before
    public void setUp(){
        /* new array before EVERY test, to not create interference */
        queue = new ArrayBlockingQueue<>(10);
        writer = new ExecuteBolt.SNSWriter(queue);
    }

    /**
     * test if consumer created on queue,
     * actually consumes from it,
     * connects to SNS and successfully
     * sends message on it
     * @throws Exception
     */
    @Test
    public void run() throws Exception {
        queue.put("Test Message");
        writer.run();

        /* wait for thread to consume */
        Thread.sleep(2000);

        /* check if connection is on */
        assertTrue(writer.isConnected());
        /* check if produced message has been consumed */
        assertTrue(queue.size() == 0);
        /* check if message successfully sent on SNS */
        assertTrue(writer.lastMessageSent());
    }

}