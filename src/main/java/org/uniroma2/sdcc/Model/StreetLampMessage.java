package org.uniroma2.sdcc.Model;

import java.io.Serializable;

/**
 * message sent by every street lamp, including:
 * street lamp info (ID,address,..) along with
 * natural light level and timestamp
 */
public class StreetLampMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private StreetLamp streetLamp;
    private NaturalLight naturalLight;
    private Long timestamp;

    public StreetLampMessage() {
    }

    public StreetLampMessage(StreetLamp streetLamp, NaturalLight naturalLightLevel, Long timestamp) {
        this.streetLamp = streetLamp;
        this.naturalLight = naturalLightLevel;
        this.timestamp = timestamp;
    }

    public StreetLamp getStreetLamp() {
        return streetLamp;
    }

    public void setStreetLamp(StreetLamp streetLamp) {
        this.streetLamp = streetLamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public NaturalLight getNaturalLight() {
        return naturalLight;
    }

    public void setNaturalLight(NaturalLight naturalLight) {
        this.naturalLight = naturalLight;
    }
}
