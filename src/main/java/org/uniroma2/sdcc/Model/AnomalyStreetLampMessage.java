package org.uniroma2.sdcc.Model;

import java.util.HashMap;

/**
 * Created by ovidiudanielbarba on 04/04/2017.
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
}
