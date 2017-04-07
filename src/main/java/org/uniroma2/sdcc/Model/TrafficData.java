package org.uniroma2.sdcc.Model;

import java.sql.Timestamp;

public class TrafficData {

    private String street;
    private Float congestionPercentage;
    private Timestamp timestamp;

    public TrafficData(
            String street, Float congestionPercentage, Timestamp timestamp) {
        this.street = street;
        this.congestionPercentage = congestionPercentage;
        this.timestamp = timestamp;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Float getCongestionPercentage() {
        return congestionPercentage;
    }

    public void setCongestionPercentage(Float congestionPercentage) {
        this.congestionPercentage = congestionPercentage;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

