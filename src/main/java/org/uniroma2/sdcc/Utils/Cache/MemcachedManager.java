package org.uniroma2.sdcc.Utils.Cache;

import net.spy.memcached.MemcachedClient;
import org.uniroma2.sdcc.Utils.HeliosLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by ovidiudanielbarba on 21/04/2017.
 */
public class MemcachedManager implements CacheManager {

    private static final String LOG_TAG = "[MemcachedManager]";
    public static final String TRAFFIC_LIST_KEY = "traffic_list";
    public static final String PARKING_LIST_KEY = "parking_list";
    public static final String CURRENT_GLOBAL_RANK = "current_global_rank";
    public static final String OLD_COUNTER = "old_counter";
    public static final String SENT_GLOBAL_RANKING = "sent_global_ranking";


    private String host;
    private Integer port;
    private MemcachedClient client;

    private boolean available;

    public MemcachedManager(String host, Integer port) {
        this.host = host;
        this.port = port;

        connect();
    }

    /**
     * connect to Memcached server
     * if connection not available,
     * set available to false
     */
    private void connect() {
        try {
            client = new MemcachedClient(new InetSocketAddress(host,port));

            if(connectionEstablished()){

                available = true;
            } else {

                available = false;
                HeliosLog.logFail(LOG_TAG,"Connection Failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Memcached doesn't immediatly detect if the connection succeded or not
     * but only by operation timeout
     * So try the connection by sending a mock value (using also the current
     * thread ID) and try to getString it back and see if connection is established
     * or not. The value has a low expiration time
     * @return
     */
    private boolean connectionEstablished() {
        String tryConnectionKey = "tryConnection" + Thread.currentThread().getId();
        String tryConnectionValue = "OK";

        try {
            client.set(tryConnectionKey, 10, tryConnectionValue);
            String received = (String) client.get(tryConnectionKey);
            return received.equals(tryConnectionValue);
        } catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean put(String key, String value) {

        if(isAvailable()){
            client.set(key, 0, value);
            return true;
        }
        return false;
    }

    @Override
    public String getString(String key) {
        String received;
        if(isAvailable()){
            received = (String) client.get(key);
            return received;
        }
        return null;
    }

    @Override
    public HashMap<Integer, Integer> getIntIntMap(String key) {
        HashMap<Integer, Integer> map;
        if(isAvailable()){
            map = (HashMap<Integer, Integer>) client.get(key);
            return map;
        }
        return null;
    }

    /**
     * @return true if it's available,
     *         false otherwise
     */
    public boolean isAvailable() {
        return available && client != null;
    }

    /**
     * close connection to Memcached
     */
    public void close(){
        client.shutdown();
    }
}
