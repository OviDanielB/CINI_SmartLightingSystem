package org.uniroma2.sdcc.Model;

import java.io.Serializable;

public class DayLightData {

    private Integer streetLampID;
    private Float value;
    private Long timestamp;

    public DayLightData(Integer streetLampID, Float value) {
        this.streetLampID = streetLampID;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public Integer getStreetLampID() {
        return streetLampID;
    }

    public void setStreetLampID(Integer streetLampID) {
        this.streetLampID = streetLampID;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
