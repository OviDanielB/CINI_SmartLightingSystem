package org.uniroma2.sdcc.ControlSystem;

import net.spy.memcached.MemcachedClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**
 * This component request every 10 seconds through Parking REST API
 * values of percentage of current parking occupation of all car parks' cells.
 */
public class ParkingSource extends TimerTask {

    private static String REST_SERVER = "localhost";
    private static int REST_PORT = 3000;
    private static String REST_URL =
            "http://" + REST_SERVER + ":" + REST_PORT + "/parking";
    org.apache.http.client.HttpClient httpClient;

    private static String MEMCACHED_HOST = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private CacheManager cache;

    public ParkingSource() {
        cache = new MemcachedManager(MEMCACHED_HOST,MEMCACHED_PORT);
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public void run() {
        String responseBody = getParkingOccupation();
        saveCurrentData(responseBody);
    }

    /**
     * Save current parking occupation information in memory.
     *
     * @param responseBody body of response to GET request
     */
    private void saveCurrentData(String responseBody) {

        if (responseBody != null) {
            cache.put(MemcachedManager.PARKING_LIST_KEY,responseBody);
        }
    }

    /**
     * Request for list of parking occupation percentages by cell ids
     *
     * @return response body to GET request
     */
    private String getParkingOccupation() {
        HttpGet httpGet = new HttpGet(REST_URL);

        // Execute HTTP GET Request to ParkingServer
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}


