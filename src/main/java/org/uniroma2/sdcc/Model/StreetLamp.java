package org.uniroma2.sdcc.Model;

import java.time.LocalDateTime;

public class StreetLamp {

    private int ID;
    private boolean on;
    private Lamp lampModel;
    private Address address;
    private int cellID; // -1 if lamp is not placed in a cell park
    private float lightIntensity;
    private float consumption;
    private LocalDateTime lifetime;

    public StreetLamp() {
    }

    public StreetLamp(int ID, boolean on, Lamp lampModel, Address address, int cellID,
                      float lightIntensity, float consumption, LocalDateTime lifetime) {
        this.ID = ID;
        this.on = on;
        this.lampModel = lampModel;
        this.address = address;
        this.cellID = cellID;
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

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
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

    public LocalDateTime getLifetime() {
        return lifetime;
    }

    public void setLifetime(LocalDateTime lifetime) {
        this.lifetime = lifetime;
    }
}
