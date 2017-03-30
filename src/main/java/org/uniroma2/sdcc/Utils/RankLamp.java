package org.uniroma2.sdcc.Utils;

import java.sql.Timestamp;
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
    private Date lifetime;
    private Timestamp timestamp;

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Date getLifetime() {
        return lifetime;
    }

    public RankLamp (int id, String address, Date lifetime, Timestamp timestamp) {
        this.id = id;
        this.address = address;
        this.lifetime = lifetime;
        this.timestamp = timestamp;
    }
}
