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
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/DELETEReq" target="_top">AWS API
 *      Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class DELETEReq implements Serializable, Cloneable, StructuredPojo {

    private Key key;

    private String tableName;

    /**
     * @param key
     */

    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * @return
     */

    public Key getKey() {
        return this.key;
    }

    /**
     * @param key
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public DELETEReq key(Key key) {
        setKey(key);
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

    public DELETEReq tableName(String tableName) {
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
        if (getKey() != null)
            sb.append("Key: ").append(getKey()).append(",");
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

        if (obj instanceof DELETEReq == false)
            return false;
        DELETEReq other = (DELETEReq) obj;
        if (other.getKey() == null ^ this.getKey() == null)
            return false;
        if (other.getKey() != null && other.getKey().equals(this.getKey()) == false)
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

        hashCode = prime * hashCode + ((getKey() == null) ? 0 : getKey().hashCode());
        hashCode = prime * hashCode + ((getTableName() == null) ? 0 : getTableName().hashCode());
        return hashCode;
    }

    @Override
    public DELETEReq clone() {
        try {
            return (DELETEReq) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        org.uniroma2.sdcc.Traffic.model.transform.DELETEReqMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
