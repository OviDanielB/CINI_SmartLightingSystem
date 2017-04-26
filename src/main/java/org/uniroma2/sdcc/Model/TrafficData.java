package org.uniroma2.sdcc.Model;

/**
 * Model of data to describe percentage of traffic level in a street
 * at a specific time.
 * This data are obtained requesting to a Traffic REST API.
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

    public boolean equals(TrafficData trafficData) {
        return this.getStreet().equals(trafficData.getStreet())
                && this.getCongestionPercentage().equals(trafficData.getCongestionPercentage())
                && this.getTimestamp().equals(trafficData.getTimestamp());
    }
}

