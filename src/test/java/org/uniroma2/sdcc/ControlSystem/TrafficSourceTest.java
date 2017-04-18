package org.uniroma2.sdcc.ControlSystem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.spy.memcached.MemcachedClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Model.TrafficData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test saving data requested in memory.
 */
public class TrafficSourceTest {

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private static MemcachedClient memcachedClient;
    private Gson gson;
    private Type listType;

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning TrafficSource Test");

        gson = new Gson();
        listType = new TypeToken<List<TrafficData>>(){}.getType();
        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveInMemory() {

        String testResponse =
                "[{'street': 'Via Cambridge', " +
                        "'congestionPercentage': 60, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}," +
                        "{'street': 'Via Politecnico', " +
                        "'congestionPercentage': 80, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}]";

        memcachedClient.set("traffic_test",3600,testResponse);

        String readFromMemory = (String) memcachedClient.get("traffic_test");

        assertEquals(testResponse, readFromMemory);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended TrafficSource Test");
    }
}