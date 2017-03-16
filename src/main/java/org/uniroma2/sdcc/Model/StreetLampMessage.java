package org.uniroma2.sdcc.Model;

/**
 * Created by ovidiudanielbarba on 16/03/2017.
 */

import java.sql.Timestamp;

/**
 * message sent by every street lamp, including:
 * street lamp info (ID,address,..) along with
 * natural light level and timestamp
 */
public class StreetLampMessage {

    private StreetLamp streetLamp;
    private NaturalLightLevel naturalLightLevel;
    private Timestamp timestamp;

    public StreetLampMessage() {
    }

    public StreetLampMessage(StreetLamp streetLamp, NaturalLightLevel naturalLightLevel, Timestamp timestamp) {
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

    public NaturalLightLevel getNaturalLightLevel() {
        return naturalLightLevel;
    }

    public void setNaturalLightLevel(NaturalLightLevel naturalLightLevel) {
        this.naturalLightLevel = naturalLightLevel;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
