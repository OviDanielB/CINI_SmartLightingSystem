package org.uniroma2.sdcc.Model;

import java.time.LocalDateTime;

/**
 * Model of a lamp that describes:
 * - identification code in the city
 * - state (on/off)
 * - bulb model of the lamp (LED/CFL/...)
 * - position
 * - identification code of the parking cell where it's placed (if any)
 * - light intensity
 * - consumption quantity
 * - date of last lamp replacement
 */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreetLamp that = (StreetLamp) o;

        if (ID != that.ID) return false;
        if (on != that.on) return false;
        if (cellID != that.cellID) return false;
        if (Float.compare(that.lightIntensity, lightIntensity) != 0) return false;
        if (Float.compare(that.consumption, consumption) != 0) return false;
        if (lampModel != that.lampModel) return false;
        if (!address.equals(that.address)) return false;
        return lifetime.equals(that.lifetime);
    }

    @Override
    public int hashCode() {
        int result = ID;
        result = 31 * result + (on ? 1 : 0);
        result = 31 * result + lampModel.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + cellID;
        result = 31 * result + (lightIntensity != +0.0f ? Float.floatToIntBits(lightIntensity) : 0);
        result = 31 * result + (consumption != +0.0f ? Float.floatToIntBits(consumption) : 0);
        result = 31 * result + lifetime.hashCode();
        return result;
    }
}
