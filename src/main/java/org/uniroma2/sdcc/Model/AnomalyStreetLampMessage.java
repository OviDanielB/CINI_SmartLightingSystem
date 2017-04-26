package org.uniroma2.sdcc.Model;

import java.util.HashMap;

/**
 * Message composed by Monitoring System about every street lamp,
 * including: street lamp info (ID,address,..) along with
 * natural light level and timestamp and additional condition
 * information evaluated according to consumption and intensity level
 * compared with the other lamps in the same street and weather condition
 * to recognize behaviour anomalies.
 */
public class AnomalyStreetLampMessage extends StreetLampMessage {

    private HashMap<MalfunctionType,Float> anomalies;
    private Long noResponseCount;

    public AnomalyStreetLampMessage() {
    }

    public AnomalyStreetLampMessage(StreetLamp streetLamp, Float naturalLightLevel, Long timestamp,
                                    HashMap<MalfunctionType,Float> anomalies, Long noResponseCount) {
        super(streetLamp, naturalLightLevel, timestamp);
        this.anomalies = anomalies;
        this.noResponseCount = noResponseCount;
    }

    public HashMap<MalfunctionType,Float> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(HashMap<MalfunctionType,Float> anomalies) {
        this.anomalies = anomalies;
    }

    public Long getNoResponseCount() {
        return noResponseCount;
    }

    public void setNoResponseCount(Long noResponseCount) {
        this.noResponseCount = noResponseCount;
    }

    public boolean equals(AnomalyStreetLampMessage anomalyStreetLampMessage) {
        return super.getStreetLamp().equals(anomalyStreetLampMessage.getStreetLamp())
                && super.getNaturalLightLevel().equals(anomalyStreetLampMessage.getNaturalLightLevel())
                && super.getTimestamp().equals(anomalyStreetLampMessage.getTimestamp())
                && this.getAnomalies().equals(anomalyStreetLampMessage.getAnomalies())
                && this.getNoResponseCount().equals(anomalyStreetLampMessage.getNoResponseCount());
    }
}
