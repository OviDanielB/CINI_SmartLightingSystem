package org.uniroma2.sdcc.Utils.Config;

import java.io.Serializable;

/**
 * Configuration for thresholds which control planning is based on.
 */
public class ControlConfig implements Serializable {

    private static final long serialVersionUID = 42L;

    private Float traffic_tolerance;
    private Float parking_tolerance;

    public Float getTraffic_tolerance() {
        return traffic_tolerance;
    }

    public void setTraffic_tolerance(Float traffic_tolerance) {
        this.traffic_tolerance = traffic_tolerance;
    }

    public Float getParking_tolerance() {
        return parking_tolerance;
    }

    public void setParking_tolerance(Float parking_tolerance) {
        this.parking_tolerance = parking_tolerance;
    }

}
