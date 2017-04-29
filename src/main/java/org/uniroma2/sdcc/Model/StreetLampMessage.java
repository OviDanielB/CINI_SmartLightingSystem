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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreetLampMessage that = (StreetLampMessage) o;

        if (Float.compare(that.naturalLightLevel, naturalLightLevel) != 0) return false;
        if (!streetLamp.equals(that.streetLamp)) return false;
        return timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = streetLamp.hashCode();
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + (naturalLightLevel != +0.0f ? Float.floatToIntBits(naturalLightLevel) : 0);
        return result;
    }

}
