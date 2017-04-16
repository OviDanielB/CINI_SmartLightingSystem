package org.uniroma2.sdcc.ControlSystem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.spy.memcached.MemcachedClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Model.ParkingData;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test request through REST API to ParkingServer to GET parking cells occupation data.
 * Test save data requested in memory.
 */
public class ParkingSourceTest {

    /* running server_parking_TEST  */
    private static String REST_SERVER = "localhost";
    private static int REST_PORT = 3000;
    private static String REST_URL =
            "http://" + REST_SERVER + ":" + REST_PORT + "/parking";

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private static MemcachedClient memcachedClient;
    private Gson gson;
    private Type listType;

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning ParkingSource Test");

        gson = new Gson();
        listType = new TypeToken<List<ParkingData>>(){}.getType();
        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Server Parking Test response constant values to a GET method.
     */
    @Test
    public void getParkingOccupation() {

        List<ParkingData> testResponse = gson.fromJson(
                "[{'cellID': 1001, " +
                        "'street': 'Via Cambridge', " +
                        "'occupationPercentage': 0.2, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}," +
                        "{'cellID': 1002, " +
                        "street: 'Via Cambridge', " +
                        "'occupationPercentage': 0.2, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}]", listType);

        org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(REST_URL);

        // Execute HTTP GET Request to ParkingServer
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            String response = httpClient.execute(httpGet, responseHandler);
            List<ParkingData> actualResponse = gson.fromJson(response, listType);

            assertEquals(testResponse.get(0).getCellID(), actualResponse.get(0).getCellID());
            assertEquals(testResponse.get(0).getStreet(), actualResponse.get(0).getStreet());
            assertEquals(testResponse.get(0).getOccupationPercentage(), actualResponse.get(0).getOccupationPercentage());
            assertEquals(testResponse.get(0).getTimestamp(), actualResponse.get(0).getTimestamp(),60);

            assertEquals(testResponse.get(1).getCellID(), actualResponse.get(1).getCellID());
            assertEquals(testResponse.get(1).getStreet(), actualResponse.get(1).getStreet());
            assertEquals(testResponse.get(1).getOccupationPercentage(), actualResponse.get(1).getOccupationPercentage());
            assertEquals(testResponse.get(1).getTimestamp(), actualResponse.get(1).getTimestamp(),60);

        } catch (IOException e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void saveInMemory() {

        String testResponse =
                "[{'cellID': 1001, " +
                        "'street': 'Via Cambridge', " +
                        "'occupationPercentage': 0.2, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}," +
                        "{'cellID': 1002, " +
                        "street: 'Via Cambridge', " +
                        "'occupationPercentage': 0.2, " +
                        "'timestamp': "+ System.currentTimeMillis() + "}]";

        memcachedClient.set("parking_test",3600,testResponse);

        String readFromMemory = (String) memcachedClient.get("parking_test");

        assertEquals(testResponse, readFromMemory);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended ParkingSource Test");
    }
}