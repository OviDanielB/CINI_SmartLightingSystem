package org.uniroma2.sdcc.Utils;

/**
 * @author emanuele
 */
public class WrappedKey {

    private Integer id;
    private String street;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof WrappedKey) {
            WrappedKey s = (WrappedKey) obj;
            return id.equals(s.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (id.hashCode());
    }
}
