package org.uniroma2.sdcc.Model;


public class TrafficData {

    private String street;
    private Float congestionPercentage;
    private Long timestamp;

    public TrafficData(String street, Float concegestionPercentage) {
        this.street = street;
        this.congestionPercentage = concegestionPercentage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Float getConcegestionPercentage() {
        return congestionPercentage;
    }

    public void setConcegestionPercentage(Float concegestionPercentage) {
        this.congestionPercentage = concegestionPercentage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

