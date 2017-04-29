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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnomalyStreetLampMessage that = (AnomalyStreetLampMessage) o;

        if (!anomalies.equals(that.anomalies)) return false;
        return noResponseCount != null ? noResponseCount.equals(that.noResponseCount) : that.noResponseCount == null;
    }

    @Override
    public int hashCode() {
        int result = anomalies.hashCode();
        result = 31 * result + (noResponseCount != null ? noResponseCount.hashCode() : 0);
        return result;
    }
}
