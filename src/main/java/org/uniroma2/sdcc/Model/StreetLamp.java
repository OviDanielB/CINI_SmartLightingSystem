package org.uniroma2.sdcc.Model;

import java.util.Date;

/**
 * Created by ovidiudanielbarba on 16/03/2017.
 */
public class StreetLamp {

    private int ID;
    private boolean on;
    private Lamp lampModel;
    private Address address;
    private float lightIntensity;
    private float consumption;
    private Date lifetime;

    public StreetLamp() {
    }

    public StreetLamp(int ID, boolean on, Lamp lampModel, Address address,
                      float lightIntensity, float consumption, Date lifetime) {
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

    public Date getLifetime() {
        return lifetime;
    }

    public void setLifetime(Date lifetime) {
        this.lifetime = lifetime;
    }
}
