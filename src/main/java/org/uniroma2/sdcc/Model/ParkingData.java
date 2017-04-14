package org.uniroma2.sdcc.Model;

/**
 * Model of query result from request to Parking REST API to obtain traffic
 * level percentage by cellID specified.
 */
public class ParkingData {

    private int cellID;
    // TODO add park street/ park ID ???
    private String street;
    private Float occupationPercentage;
    private Long timestamp;

    public ParkingData(
            int cellID, Float occupationPercentage) {
        this.cellID = cellID;
        this.occupationPercentage = occupationPercentage;
        this.timestamp = System.currentTimeMillis();
    }

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public Float getOccupationPercentage() {
        return occupationPercentage;
    }

    public void setOccupationPercentage(Float occupationPercentage) {
        this.occupationPercentage = occupationPercentage;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet() {
        return this.street;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

