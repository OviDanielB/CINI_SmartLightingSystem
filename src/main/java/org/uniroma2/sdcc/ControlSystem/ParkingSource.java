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


    public static void main(String[] args) throws IOException, InterruptedException {

        MemcachedClient memcachedClient =
                new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));

        // asking forever (every 10 seconds) traffic information
        while (true) {

            org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(REST_URL);

            // Execute HTTP GET Request to ParkingServer
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(httpGet, responseHandler);

            memcachedClient.set("parking_list", 0, responseBody);

            System.out.println(" [ParkingSource] Received: " + responseBody + "\n");

            // requesting for data every 10 seconds
            sleep(10000);
        }
    }
}


