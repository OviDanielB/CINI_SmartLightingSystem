package org.uniroma2.sdcc.Model;

import java.util.List;

/**
 * Created by ovidiudanielbarba on 04/04/2017.
 */
public class AnomalyStreetLampMessage extends StreetLampMessage{

    private List<MalfunctionType> malfunctionTypes;
    private Long noResponseCount;

    public AnomalyStreetLampMessage() {
    }

    public AnomalyStreetLampMessage(StreetLamp streetLamp, Float naturalLightLevel, Long timestamp,
                                    List<MalfunctionType> malfunctionTypes, Long noResponseCount) {
        super(streetLamp, naturalLightLevel, timestamp);
        this.malfunctionTypes = malfunctionTypes;
        this.noResponseCount = noResponseCount;
    }

    public List<MalfunctionType> getMalfunctionTypes() {
        return malfunctionTypes;
    }

    public void setMalfunctionTypes(List<MalfunctionType> malfunctionTypes) {
        this.malfunctionTypes = malfunctionTypes;
    }

    public Long getNoResponseCount() {
        return noResponseCount;
    }

    public void setNoResponseCount(Long noResponseCount) {
        this.noResponseCount = noResponseCount;
    }
}
