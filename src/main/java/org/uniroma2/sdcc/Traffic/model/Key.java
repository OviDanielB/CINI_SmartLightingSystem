/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model;

import java.io.Serializable;
import javax.annotation.Generated;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.protocol.ProtocolMarshaller;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/Key" target="_top">AWS API
 *      Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class Key implements Serializable, Cloneable, StructuredPojo {

    private String city;

    private String street;

    /**
     * @param city
     */

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return
     */

    public String getCity() {
        return this.city;
    }

    /**
     * @param city
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Key city(String city) {
        setCity(city);
        return this;
    }

    /**
     * @param street
     */

    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return
     */

    public String getStreet() {
        return this.street;
    }

    /**
     * @param street
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Key street(String street) {
        setStreet(street);
        return this;
    }

    /**
     * Returns a string representation of this object; useful for testing and debugging.
     *
     * @return A string representation of this object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (getCity() != null)
            sb.append("City: ").append(getCity()).append(",");
        if (getStreet() != null)
            sb.append("Street: ").append(getStreet());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof Key == false)
            return false;
        Key other = (Key) obj;
        if (other.getCity() == null ^ this.getCity() == null)
            return false;
        if (other.getCity() != null && other.getCity().equals(this.getCity()) == false)
            return false;
        if (other.getStreet() == null ^ this.getStreet() == null)
            return false;
        if (other.getStreet() != null && other.getStreet().equals(this.getStreet()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getCity() == null) ? 0 : getCity().hashCode());
        hashCode = prime * hashCode + ((getStreet() == null) ? 0 : getStreet().hashCode());
        return hashCode;
    }

    @Override
    public Key clone() {
        try {
            return (Key) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        org.uniroma2.sdcc.Traffic.model.transform.KeyMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
