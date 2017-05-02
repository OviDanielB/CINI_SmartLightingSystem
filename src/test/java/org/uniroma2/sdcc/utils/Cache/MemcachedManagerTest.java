package org.uniroma2.sdcc.utils.Cache;

import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;

import static org.junit.Assert.*;

/**
 * Test for Memcached connection,
 * put and getString operation
 *
 * YOU MUST HAVE AN ACTIVE MEMCACHED SERVER
 * ON localhost:11211 ELSE THE TEST PASSES
 * WITHOUT CHECKING IF METHODS ACT ACCORDINGLY
 */
public class MemcachedManagerTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    CacheManager cache;

    @Before
    public void setUp(){

        cache = new MemcachedManager();
    }

    @Test
    public void putAndGet() throws Exception {

        if(cache.put(TEST_KEY,TEST_VALUE)){
            String received  = cache.getString(TEST_KEY);
            if(received != null){
                assertTrue(received.equals(TEST_VALUE));
            }
        }

        cache.close();

    }

}