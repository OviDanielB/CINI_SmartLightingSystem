/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model;

import java.io.Serializable;
import javax.annotation.Generated;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.protocol.ProtocolMarshaller;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform.ItemMarshaller;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/Item" target="_top">AWS API
 *      Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class Item implements Serializable, Cloneable, StructuredPojo {

    private String city;

    private String street;

    private Double trafficPerc;

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

    public Item city(String city) {
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

    public Item street(String street) {
        setStreet(street);
        return this;
    }

    /**
     * @param trafficPerc
     */

    public void setTrafficPerc(Double trafficPerc) {
        this.trafficPerc = trafficPerc;
    }

    /**
     * @return
     */

    public Double getTrafficPerc() {
        return this.trafficPerc;
    }

    /**
     * @param trafficPerc
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public Item trafficPerc(Double trafficPerc) {
        setTrafficPerc(trafficPerc);
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
            sb.append("Street: ").append(getStreet()).append(",");
        if (getTrafficPerc() != null)
            sb.append("TrafficPerc: ").append(getTrafficPerc());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof Item == false)
            return false;
        Item other = (Item) obj;
        if (other.getCity() == null ^ this.getCity() == null)
            return false;
        if (other.getCity() != null && other.getCity().equals(this.getCity()) == false)
            return false;
        if (other.getStreet() == null ^ this.getStreet() == null)
            return false;
        if (other.getStreet() != null && other.getStreet().equals(this.getStreet()) == false)
            return false;
        if (other.getTrafficPerc() == null ^ this.getTrafficPerc() == null)
            return false;
        if (other.getTrafficPerc() != null && other.getTrafficPerc().equals(this.getTrafficPerc()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getCity() == null) ? 0 : getCity().hashCode());
        hashCode = prime * hashCode + ((getStreet() == null) ? 0 : getStreet().hashCode());
        hashCode = prime * hashCode + ((getTrafficPerc() == null) ? 0 : getTrafficPerc().hashCode());
        return hashCode;
    }

    @Override
    public Item clone() {
        try {
            return (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        ItemMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
