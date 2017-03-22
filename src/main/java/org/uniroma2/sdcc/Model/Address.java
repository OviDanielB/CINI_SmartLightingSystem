package org.uniroma2.sdcc.Model;

/**
 * Created by ovidiudanielbarba on 16/03/2017.
 */

/**
 * defines a city address
 */
public class Address {

    private String name;
    private AddressType addressType;
    private int number;
    private AddressNumberType numberType;

    public Address() {
    }

    public Address(AddressType addressType, String name, int number, AddressNumberType numberType) {
        this.addressType = addressType;
        this.name = name;
        this.number = number;
        this.numberType = numberType;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public AddressNumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(AddressNumberType numberType) {
        this.numberType = numberType;
    }

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", addressType=" + addressType +
                ", number=" + number +
                ", numberType=" + numberType +
                '}';
    }
}
