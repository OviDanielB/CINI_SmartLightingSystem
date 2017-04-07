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
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/POSTReq" target="_top">AWS API
 *      Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class POSTReq implements Serializable, Cloneable, StructuredPojo {

    private Item item;

    private String tableName;

    /**
     * @param item
     */

    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @return
     */

    public Item getItem() {
        return this.item;
    }

    /**
     * @param item
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public POSTReq item(Item item) {
        setItem(item);
        return this;
    }

    /**
     * @param tableName
     */

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return
     */

    public String getTableName() {
        return this.tableName;
    }

    /**
     * @param tableName
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public POSTReq tableName(String tableName) {
        setTableName(tableName);
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
        if (getItem() != null)
            sb.append("Item: ").append(getItem()).append(",");
        if (getTableName() != null)
            sb.append("TableName: ").append(getTableName());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof POSTReq == false)
            return false;
        POSTReq other = (POSTReq) obj;
        if (other.getItem() == null ^ this.getItem() == null)
            return false;
        if (other.getItem() != null && other.getItem().equals(this.getItem()) == false)
            return false;
        if (other.getTableName() == null ^ this.getTableName() == null)
            return false;
        if (other.getTableName() != null && other.getTableName().equals(this.getTableName()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getItem() == null) ? 0 : getItem().hashCode());
        hashCode = prime * hashCode + ((getTableName() == null) ? 0 : getTableName().hashCode());
        return hashCode;
    }

    @Override
    public POSTReq clone() {
        try {
            return (POSTReq) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        org.uniroma2.sdcc.Traffic.model.transform.POSTReqMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
