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

    public static void main(String[] args) throws IOException, InterruptedException {

        MemcachedClient memcachedClient =
                new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        Gson gson = new Gson();
        StreetTrafficREST streetTrafficREST = new StreetTrafficREST();

        while (true) {

            List<TrafficData> street_list = streetTrafficREST.getAllCityStreetsTraffic();
            String json_street_list = gson.toJson(street_list);

            memcachedClient.set("traffic_list", 0, json_street_list);

            System.out.println("Received: " + json_street_list + "\n");

            // requesting for data every 10 seconds
            sleep(10000);
        }
    }
}
