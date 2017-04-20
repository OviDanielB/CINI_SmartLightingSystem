package org.uniroma2.sdcc.ControlSystem;

import com.google.gson.Gson;
import net.spy.memcached.MemcachedClient;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.StreetTrafficREST;
import org.uniroma2.sdcc.Utils.JSONConverter;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**
 * This component request every 10 seconds through Traffic REST API
 * values of percentage of current traffic level along a street.
 */
public class TrafficSource extends TimerTask {

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;
    private static MemcachedClient memcachedClient;

    private static StreetTrafficREST streetTrafficREST;

    public TrafficSource() {
    }

    @Override
    public void run() {

        initialization();
        String json_street_list = getTrafficData();

        saveCurrentData(json_street_list);
    }

    /*
    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {

            String json_street_list = getTrafficData();

            saveCurrentData(json_street_list);

            // requesting for data every 10 seconds
            sleep(10000);
        }
    } */

    /**
     * Save current traffic level information in memory.
     *
     * @param json_street_list body of response to GET request
     */
    private void saveCurrentData(String json_street_list) {

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
    private String getTrafficData() {

        List<TrafficData> street_list = streetTrafficREST.getAllCityStreetsTraffic();

        if (street_list != null) {
            return JSONConverter.fromTrafficDataList(street_list);
        }
        return null;
    }

    /**
     * Initialize data
     */
    private void initialization() {

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        streetTrafficREST = new StreetTrafficREST();
    }


}
