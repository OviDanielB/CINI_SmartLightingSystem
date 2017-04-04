package org.uniroma2.sdcc.Model;

/**
 * Created by ovidiudanielbarba on 30/03/2017.
 */
public enum MalfunctionType {
    WEATHER(1),
    DAMAGED_BULB(2),
    LIGHT_INTENSITY_ANOMALY(3),
    NOT_RESPONDING(4),
    NONE(0);

    private Integer code;

    MalfunctionType(Integer type) {
        this.code = type;
    }

    public Integer getCode() {
        return code;
    }
}
