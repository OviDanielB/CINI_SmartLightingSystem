package org.uniroma2.sdcc.Utils.Cache;

import java.util.TimerTask;

/**
 * Periodic (TimerTask) Thread with cache connection
 */
public abstract class MemcachedPeriodicUpdater extends TimerTask {

    protected CacheManager cache;

    public MemcachedPeriodicUpdater() {
        cache = new MemcachedManager();
    }
}
