package org.uniroma2.sdcc.Model;

import java.io.Serializable;

public class StreetLamp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer ID;
    private boolean on;
    private Lamp lampModel;
    private Address address;
    private Float lightIntensity;
    private Float consumption;
    private String lifetime;
    private Long timestamp;


    public StreetLamp() {
    }

    public StreetLamp(int ID, boolean on, Lamp lampModel, Address address,
                      Float lightIntensity, Float consumption, String lifetime) {
        this.ID = ID;
        this.on = on;
        this.lampModel = lampModel;
        this.address = address;
        this.lightIntensity = lightIntensity;
        this.consumption = consumption;
        this.lifetime = lifetime;
        timestamp = System.currentTimeMillis();
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Lamp getLampModel() {
        return lampModel;
    }

    public void setLampModel(Lamp lampModel) {
        this.lampModel = lampModel;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Float getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(Float lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public Float getConsumption() {
        return consumption;
    }

    public void setConsumption(Float consumption) {
        this.consumption = consumption;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
