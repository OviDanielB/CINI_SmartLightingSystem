package org.uniroma2.sdcc.Model;

import java.io.Serializable;

public class NaturalLight implements Serializable {

    private static final long serialVersionUID = 1L;

    /* percentage */
    private Float level;

    public NaturalLight() {
    }

    public NaturalLight(Float naturalLightLevel) {
        this.level = naturalLightLevel;
    }

    public Float getLevel() {
        return level;
    }

    public void setLevel(Float level) {
        this.level = level;
    }
}
