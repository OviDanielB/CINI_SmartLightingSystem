package org.uniroma2.sdcc.ControlSystem;

import com.google.gson.Gson;
import net.spy.memcached.MemcachedClient;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.StreetTrafficREST;

import java.io.*;
import java.net.*;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * This component request every 10 seconds through Traffic REST API
 * values of percentage of current traffic level along a street.
 */
public class TrafficSource {

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private static MemcachedClient memcachedClient;

    private static Gson gson;
    private static StreetTrafficREST streetTrafficREST;

    public static void main(String[] args) throws IOException, InterruptedException {

        initialization();

        while (true) {

            String json_street_list = getTrafficData();

            saveCurrentData(json_street_list);

            // requesting for data every 10 seconds
            sleep(10000);
        }
    }

    /**
     * Save current traffic level information in memory.
     *
     * @param json_street_list body of response to GET request
     */
    private static void saveCurrentData(String json_street_list) {

        if (json_street_list != null) {
            // save in memory
            memcachedClient.set("traffic_list", 0, json_street_list);

            System.out.println(" [TrafficSource] Received: " + json_street_list + "\n");
        }
    }

    /**
     * GET Request through REST API for street traffic data.
     *
     * @return JSON list traffic data fro all streets
     */
    private static String getTrafficData() {

        List<TrafficData> street_list = streetTrafficREST.getAllCityStreetsTraffic();

        if (street_list != null) {
            return gson.toJson(street_list);
        }
        return null;
    }

    /**
     * Initialize data
     */
    private static void initialization() {

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        gson = new Gson();
        streetTrafficREST = new StreetTrafficREST();
    }
}
