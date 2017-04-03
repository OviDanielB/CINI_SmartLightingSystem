package org.uniroma2.sdcc.Utils;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

/**
 * This class tracks the time-since-last-modify of a "thing" in a rolling fashion.
 * <p>
 * For example, create a 5-slot tracker to track the five most recent time-since-last-modify.
 * <p>
 * You must manually "mark" that the "something" that you want to track -- in terms of modification times -- has just
 * been modified.
 */
public class NthLastModifiedTimeTracker {

    private final CircularFifoBuffer lastModifiedTimesMillis;

    public NthLastModifiedTimeTracker(int numTimesToTrack) {
        if (numTimesToTrack < 1) {
            throw new IllegalArgumentException("numTimesToTrack must be greater than zero (you requested "
                    + numTimesToTrack + ")");
        }
        lastModifiedTimesMillis = new CircularFifoBuffer(numTimesToTrack);
        initLastModifiedTimesMillis();
    }

    private void initLastModifiedTimesMillis() {
        long nowCached = now();
        for (int i = 0; i < lastModifiedTimesMillis.maxSize(); i++) {
            lastModifiedTimesMillis.add(nowCached);
        }
    }

    private long now() {
        return System.currentTimeMillis() / 1000;
    }

    public int secondsSinceOldestModification() {
        long modifiedTimeMillis = (Long) lastModifiedTimesMillis.get();
        return (int) (now() - modifiedTimeMillis);
    }

    public void markAsModified() {
        updateLastModifiedTime();
    }

    private void updateLastModifiedTime() {
        lastModifiedTimesMillis.add(now());
    }

    public Long lastModieficationTime() {
        return (Long) lastModifiedTimesMillis.get();
    }
}
