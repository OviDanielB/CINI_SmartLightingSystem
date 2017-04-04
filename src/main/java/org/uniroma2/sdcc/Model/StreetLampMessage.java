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

    public static String MALFUNCTIONS_TYPE = "malfunctionsType";
    public static String STREET_LAMP_MSG = "streetLampMessage";
    public static String JSON_STRING = "jsonString";
    public static String ID = "id";
    public static String ADDRESS = "address";
    public static String ON = "on";
    public static String LAMP_MODEL = "model";
    public static String CONSUMPTION = "consumption";
    public static String INTENSITY = "intensity";
    public static String LIFETIME = "lifetime";
    public static String NATURAL_LIGHT_LEVEL = "naturalLightLevel";
    public static String TIMESTAMP = "timestamp";

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
}
