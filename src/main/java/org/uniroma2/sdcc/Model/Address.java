package org.uniroma2.sdcc.Model;

import java.io.Serializable;

/**
 * Define a city address composed by street or square name
 * and number that can be a civic number or a kilometer indication.
 * (e.i. "Via Tuscolana 4500", "Via Politecnico 1")
 */
public class Address implements Serializable {

    static final Long serialVersionUID = 1L;


    private String name;
    private int number;
    private AddressNumberType numberType;

    public Address() {
    }

    public Address(String name, int number, AddressNumberType numberType) {
        this.name = name;
        this.number = number;
        this.numberType = numberType;
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
                ", number=" + number +
                ", numberType=" + numberType +
                '}';
    }

    public boolean equals(Address address) {
        return this.getName().equals(address.getName())
                && this.getNumberType().equals(address.getNumberType())
                && this.getNumber() == address.getNumber();
    }

}
