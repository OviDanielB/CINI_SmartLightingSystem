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
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/ExpressionAttributeValues"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class ExpressionAttributeValues implements Serializable, Cloneable, StructuredPojo {

    private Double r;

    /**
     * @param r
     */

    public void setR(Double r) {
        this.r = r;
    }

    /**
     * @return
     */

    public Double getR() {
        return this.r;
    }

    /**
     * @param r
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public ExpressionAttributeValues r(Double r) {
        setR(r);
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
        if (getR() != null)
            sb.append("R: ").append(getR());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof ExpressionAttributeValues == false)
            return false;
        ExpressionAttributeValues other = (ExpressionAttributeValues) obj;
        if (other.getR() == null ^ this.getR() == null)
            return false;
        if (other.getR() != null && other.getR().equals(this.getR()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getR() == null) ? 0 : getR().hashCode());
        return hashCode;
    }

    @Override
    public ExpressionAttributeValues clone() {
        try {
            return (ExpressionAttributeValues) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        org.uniroma2.sdcc.Traffic.model.transform.ExpressionAttributeValuesMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
