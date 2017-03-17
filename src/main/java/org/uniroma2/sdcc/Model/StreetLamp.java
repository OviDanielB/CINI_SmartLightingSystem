package org.uniroma2.sdcc.Model;

import org.joda.time.DateTime;

import java.io.Serializable;

public class StreetLamp implements Serializable{

    private static final long serialVersionUID = 1L;

    private Integer ID;
    private boolean on;
    private Lamp lampModel;
    private Address address;
    private Float lightIntensity;
    private Float consumption;
    private DateTime lifetime;

    public StreetLamp() {
    }

    public StreetLamp(int ID, boolean on, Lamp lampModel, Address address,
                      Float lightIntensity, Float consumption, DateTime lifetime) {
        this.ID = ID;
        this.on = on;
        this.lampModel = lampModel;
        this.address = address;
        this.lightIntensity = lightIntensity;
        this.consumption = consumption;
        this.lifetime = lifetime;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
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

    public float getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(float lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public float getConsumption() {
        return consumption;
    }

    public void setConsumption(float consumption) {
        this.consumption = consumption;
    }

    public DateTime getLifetime() {
        return lifetime;
    }

    public void setLifetime(DateTime lifetime) {
        this.lifetime = lifetime;
    }
}
