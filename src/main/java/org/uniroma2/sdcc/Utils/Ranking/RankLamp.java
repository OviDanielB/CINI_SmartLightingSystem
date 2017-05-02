package org.uniroma2.sdcc.Utils.Ranking;

import org.uniroma2.sdcc.Model.Address;

import java.time.LocalDateTime;

/**
 * Define the ranking object to be sorted, described by ID, address, lifetime and timestamp
 * of every lamp which has been replaced more than LIFETIME_THRESHOLD days ago.
 */
public class RankLamp {

    private int id;
    private Address address;
    private LocalDateTime lifetime;
    private Long timestamp;


    public int getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getLifetime() {
        return lifetime;
    }

    public RankLamp (int id, Address address, LocalDateTime lifetime, Long timestamp) {
        this.id = id;
        this.address = address;
        this.lifetime = lifetime;
        this.timestamp = timestamp;
    }

    public boolean equals(RankLamp rankLamp) {
        return this.getId() == rankLamp.getId()
                && this.getAddress().equals(rankLamp.getAddress())
                && this.getLifetime().equals(rankLamp.getLifetime())
                && this.getTimestamp().equals(rankLamp.getTimestamp());
    }
}
