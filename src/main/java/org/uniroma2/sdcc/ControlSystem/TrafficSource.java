package org.uniroma2.sdcc.ControlSystem;

import net.spy.memcached.MemcachedClient;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.StreetTrafficREST;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.HeliosLog;
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

    private static final String LOG_TAG = "[TrafficSource]";

    private static final String MEMCACHED_HOST = "localhost";
    private static final int MEMCACHED_PORT = 11211;
    private CacheManager cache;

    private static StreetTrafficREST streetTrafficREST;

    public TrafficSource() {
        cache = new MemcachedManager(MEMCACHED_HOST,MEMCACHED_PORT);
        streetTrafficREST = new StreetTrafficREST();
    }

    @Override
    public void run() {

        String json_street_list = getTrafficData();
        saveCurrentData(json_street_list);
    }


    /**
     * Save current traffic level information
     * (got from Traffic REST API) in memory.
     * @param json_street_list body of response to GET request
     */
    private void saveCurrentData(String json_street_list) {

        if (json_street_list != null) {

            cache.put(MemcachedManager.TRAFFIC_LIST_KEY,json_street_list);
        } else {

            HeliosLog.logFail(LOG_TAG,"Attempting to insert null object");
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





}
