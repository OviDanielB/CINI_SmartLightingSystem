package org.uniroma2.sdcc.Utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Define the ranking object to be sorted, described by ID, address, lifetime and timestamp
 * of every lamp which has been replaced more than LIFETIME_THRESHOLD days ago.
 */
public class RankLamp {

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    private int id;
    private String address;
    private LocalDateTime lifetime;
    private Long timestamp;

    public Long getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getLifetime() {
        return lifetime;
    }

    public RankLamp (int id, String address, LocalDateTime lifetime, Long timestamp) {
        this.id = id;
        this.address = address;
        this.lifetime = lifetime;
        this.timestamp = timestamp;
    }
}
