package org.uniroma2.sdcc.Utils.Cache;

/**
 * Defines methods for basic caching operations
 * like get and put. User interface instead of implementing
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
     * Basic get Value from cache
     * @param key Key to search
     * @return Value as String if present in cache,
     *         null otherwise
     */
    String get(String key);

    /**
     * close connection to cache
     */
    void close();
}
