package org.uniroma2.sdcc.Utils.Cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class with connection to Memcached and constructor with
 * a hashmap (concurrent one) with (key, value) to write to cache
 */
public class MemcachedWriterRunnable extends Thread {

    protected CacheManager cache;
    protected ConcurrentHashMap<String, String> keyValue;

    public MemcachedWriterRunnable(ConcurrentHashMap<String, String> keyValue) {
        this.keyValue = keyValue;
        cache = new MemcachedManager();
    }

    @Override
    public void run() {
        /* avoid consistency problems */
        while (keyValue != null){

            keyValue.entrySet().forEach((e) -> {
                cache.put(e.getKey(), e.getValue());
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
