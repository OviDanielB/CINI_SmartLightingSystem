package org.uniroma2.sdcc.Model;

/**
 * Message sent by every street lamp, including:
 * street lamp info (ID,address,..) along with
 * natural light level and timestamp
 */
public class StreetLampMessage {

    private StreetLamp streetLamp;
    private Long timestamp;
    private float naturalLightLevel;


    public StreetLampMessage() {
    }

    public StreetLampMessage(StreetLamp streetLamp, Float naturalLightLevel, Long timestamp) {
        this.streetLamp = streetLamp;
        this.naturalLightLevel = naturalLightLevel;
        this.timestamp = timestamp;
    }

    public StreetLamp getStreetLamp() {
        return streetLamp;
    }

    public void setStreetLamp(StreetLamp streetLamp) {
        this.streetLamp = streetLamp;
    }

    public Float getNaturalLightLevel() {
        return naturalLightLevel;
    }

    public void setNaturalLightLevel(Float naturalLightLevel) {
        this.naturalLightLevel = naturalLightLevel;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean equals(StreetLampMessage streetLampMessage) {
        return this.getStreetLamp().equals(streetLampMessage.getStreetLamp())
                && this.getNaturalLightLevel().equals(streetLampMessage.getNaturalLightLevel())
                && this.getTimestamp().equals(streetLampMessage.getTimestamp());
    }
}
