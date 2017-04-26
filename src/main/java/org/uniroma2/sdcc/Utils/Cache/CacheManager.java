package org.uniroma2.sdcc.Utils.Cache;

import java.util.HashMap;
import java.util.Objects;

/**
 * Defines methods for basic caching operations
 * like getString and put. User interface instead of implementing
 * class because caching system could change
 */
public interface CacheManager {

    /**
     * Basic put (Key, Value) in cache
     * @param key Key to store
     * @param value Value to store
     * @return true if operation completed correctly,
     *         false otherwise
     */
    boolean put(String key, String value);

    /**
     * Basic Get String Value from cache
     * @param key Key to search
     * @return Value as String if present in cache,
     *         null otherwise
     */
    String getString(String key);

    /**
     * Basic get HashMap<Integer, Integer> from cache
     * @param key Key to search
     * @return Values as HashMap if present in cache,
     *         null otherwise
     */
    HashMap<Integer, Integer> getIntIntMap(String key);

    /**
     * close connection to cache
     */
    void close();
}
