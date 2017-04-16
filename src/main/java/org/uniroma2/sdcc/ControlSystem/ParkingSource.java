package org.uniroma2.sdcc.ControlSystem;

import net.spy.memcached.MemcachedClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.Thread.sleep;

/**
 * This component request every 10 seconds through Parking REST API
 * values of percentage of current parking occupation of all car parks' cells.
 */
public class ParkingSource {

    private static String REST_SERVER = "localhost";
    private static int REST_PORT = 3000;
    private static String REST_URL =
            "http://" + REST_SERVER + ":" + REST_PORT + "/parking";

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private static MemcachedClient memcachedClient;


    public static void main(String[] args) throws IOException, InterruptedException {

        initializeMem();

        // asking forever (every 10 seconds) parking information
        while (true) {

            String responseBody = getParkingOccupation();

            saveCurrentData(responseBody);

            // requesting for data every 10 seconds
            sleep(10000);
        }
    }

    /**
     * Initialize memory client.
     */
    private static void initializeMem() {

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save current parking occupation information in memory.
     *
     * @param responseBody body of response to GET request
     */
    private static void saveCurrentData(String responseBody) {

        if ((responseBody = getParkingOccupation()) != null) {
            // save in memory
            memcachedClient.set("parking_list", 0, responseBody);

            System.out.println(" [ParkingSource] Received: " + responseBody + "\n");
        }
    }

    /**
     * Request for list of parking occupation percentages by cell ids
     *
     * @return response body to GET request
     */
    private static String getParkingOccupation() {

        org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
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


