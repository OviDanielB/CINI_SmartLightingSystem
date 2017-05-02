package org.uniroma2.sdcc.Utils.MOM;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests
 */
public class RabbitPubSubManagerTest {

    private static final String HOST = "localhost";
    private static final Integer PORT = 5673;
    private static final String EXCHANGE_NAME = "dashboard_exchange";

    private static final String ROUTING_KEY = "dashboard.anomalies";

    private PubSubManager manager;

    @Before
    public void setUp(){
        manager = new RabbitPubSubManager(HOST,PORT,EXCHANGE_NAME, QueueClientType.PRODUCER);
    }

    @Test
    public void publish() throws Exception {

        assertTrue(manager.publish(ROUTING_KEY,"Hello"));

    }

    /**
     * tests if connection is available with parameters
     * taken from config file and successfully sends messages
     * @throws Exception
     */
    @Test
    public void publishConfigFile() throws Exception {

        manager = new RabbitPubSubManager();

        assertTrue(manager.publish(ROUTING_KEY,"Test"));
    }

}