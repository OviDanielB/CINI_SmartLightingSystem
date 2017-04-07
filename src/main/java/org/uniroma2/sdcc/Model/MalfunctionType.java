package org.uniroma2.sdcc.Model;

/**
 * Created by ovidiudanielbarba on 30/03/2017.
 */
public enum MalfunctionType {
    // *_LESS defecting value, anomaly gap from correct value has to be added
    // *_MORE excess value, anomaly gap from correct value has to be 'sottratto'
    WEATHER_LESS(1),
    DAMAGED_BULB(2),
    LIGHT_INTENSITY_ANOMALY_LESS(3),
    WEATHER_MORE(5),
    LIGHT_INTENSITY_ANOMALY_MORE(6),
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
