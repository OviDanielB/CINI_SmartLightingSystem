package org.uniroma2.sdcc.Model;

/**
 * Created by ovidiudanielbarba on 16/03/2017.
 */
public class NaturalLightLevel {

    /* percentage */
    float naturalLightLevel;

    public NaturalLightLevel() {
    }

    public NaturalLightLevel(float naturalLightLevel) {
        this.naturalLightLevel = naturalLightLevel;
    }

    public float getNaturalLightLevel() {
        return naturalLightLevel;
    }

    public void setNaturalLightLevel(float naturalLightLevel) {
        this.naturalLightLevel = naturalLightLevel;
    }
}
