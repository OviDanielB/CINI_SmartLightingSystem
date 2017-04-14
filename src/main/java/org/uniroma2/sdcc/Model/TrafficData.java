package org.uniroma2.sdcc.Model;

/**
 * Model of query result from request to Traffic REST API to obtain traffic
 * level percentage by street specified.
 */
public class TrafficData {

    private String street;
    private Float congestionPercentage;
    private Long timestamp;

    public TrafficData(
            String street, Float congestionPercentage) {
        this.street = street;
        this.congestionPercentage = congestionPercentage;
        this.timestamp = System.currentTimeMillis();
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

