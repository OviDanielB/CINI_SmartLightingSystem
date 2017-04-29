package org.uniroma2.sdcc.Model;

/**
 * Model of query result from request to Parking REST API to obtain traffic
 * level percentage by cellID specified.
 */
public class ParkingData {

    private int cellID;
    // TODO add park ID ???
    private Float occupationPercentage;
    private Long timestamp;

    public ParkingData(
            int cellID, String street, Float occupationPercentage) {
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingData that = (ParkingData) o;

        if (cellID != that.cellID) return false;
        if (!occupationPercentage.equals(that.occupationPercentage)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = cellID;
        result = 31 * result + occupationPercentage.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}

