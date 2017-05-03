/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model;

import java.io.Serializable;
import javax.annotation.Generated;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.protocol.ProtocolMarshaller;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform.GETTrafficResponseMarshaller;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/GETTrafficResponse"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class GETTrafficResponse implements Serializable, Cloneable, StructuredPojo {

    private java.util.List<ItemsItem> items;

    /**
     * @return
     */

    public java.util.List<ItemsItem> getItems() {
        return items;
    }

    /**
     * @param items
     */

    public void setItems(java.util.Collection<ItemsItem> items) {
        if (items == null) {
            this.items = null;
            return;
        }

        this.items = new java.util.ArrayList<ItemsItem>(items);
    }

    /**
     * <p>
     * <b>NOTE:</b> This method appends the values to the existing list (if any). Use
     * {@link #setItems(java.util.Collection)} or {@link #withItems(java.util.Collection)} if you want to override the
     * existing values.
     * </p>
     * 
     * @param items
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public GETTrafficResponse items(ItemsItem... items) {
        if (this.items == null) {
            setItems(new java.util.ArrayList<ItemsItem>(items.length));
        }
        for (ItemsItem ele : items) {
            this.items.add(ele);
        }
        return this;
    }

    /**
     * @param items
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public GETTrafficResponse items(java.util.Collection<ItemsItem> items) {
        setItems(items);
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
        if (getItems() != null)
            sb.append("Items: ").append(getItems());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof GETTrafficResponse == false)
            return false;
        GETTrafficResponse other = (GETTrafficResponse) obj;
        if (other.getItems() == null ^ this.getItems() == null)
            return false;
        if (other.getItems() != null && other.getItems().equals(this.getItems()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getItems() == null) ? 0 : getItems().hashCode());
        return hashCode;
    }

    @Override
    public GETTrafficResponse clone() {
        try {
            return (GETTrafficResponse) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        GETTrafficResponseMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
