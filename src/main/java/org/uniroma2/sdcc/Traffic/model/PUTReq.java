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
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PUTReq" target="_top">AWS API
 *      Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PUTReq implements Serializable, Cloneable, StructuredPojo {

    private ExpressionAttributeValues expressionAttributeValues;

    private Key key;

    private String returnValues;

    private String tableName;

    private String updateExpression;

    /**
     * @param expressionAttributeValues
     */

    public void setExpressionAttributeValues(ExpressionAttributeValues expressionAttributeValues) {
        this.expressionAttributeValues = expressionAttributeValues;
    }

    /**
     * @return
     */

    public ExpressionAttributeValues getExpressionAttributeValues() {
        return this.expressionAttributeValues;
    }

    /**
     * @param expressionAttributeValues
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public PUTReq expressionAttributeValues(ExpressionAttributeValues expressionAttributeValues) {
        setExpressionAttributeValues(expressionAttributeValues);
        return this;
    }

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

    public PUTReq key(Key key) {
        setKey(key);
        return this;
    }

    /**
     * @param returnValues
     */

    public void setReturnValues(String returnValues) {
        this.returnValues = returnValues;
    }

    /**
     * @return
     */

    public String getReturnValues() {
        return this.returnValues;
    }

    /**
     * @param returnValues
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public PUTReq returnValues(String returnValues) {
        setReturnValues(returnValues);
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

    public PUTReq tableName(String tableName) {
        setTableName(tableName);
        return this;
    }

    /**
     * @param updateExpression
     */

    public void setUpdateExpression(String updateExpression) {
        this.updateExpression = updateExpression;
    }

    /**
     * @return
     */

    public String getUpdateExpression() {
        return this.updateExpression;
    }

    /**
     * @param updateExpression
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public PUTReq updateExpression(String updateExpression) {
        setUpdateExpression(updateExpression);
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
        if (getExpressionAttributeValues() != null)
            sb.append("ExpressionAttributeValues: ").append(getExpressionAttributeValues()).append(",");
        if (getKey() != null)
            sb.append("Key: ").append(getKey()).append(",");
        if (getReturnValues() != null)
            sb.append("ReturnValues: ").append(getReturnValues()).append(",");
        if (getTableName() != null)
            sb.append("TableName: ").append(getTableName()).append(",");
        if (getUpdateExpression() != null)
            sb.append("UpdateExpression: ").append(getUpdateExpression());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof PUTReq == false)
            return false;
        PUTReq other = (PUTReq) obj;
        if (other.getExpressionAttributeValues() == null ^ this.getExpressionAttributeValues() == null)
            return false;
        if (other.getExpressionAttributeValues() != null && other.getExpressionAttributeValues().equals(this.getExpressionAttributeValues()) == false)
            return false;
        if (other.getKey() == null ^ this.getKey() == null)
            return false;
        if (other.getKey() != null && other.getKey().equals(this.getKey()) == false)
            return false;
        if (other.getReturnValues() == null ^ this.getReturnValues() == null)
            return false;
        if (other.getReturnValues() != null && other.getReturnValues().equals(this.getReturnValues()) == false)
            return false;
        if (other.getTableName() == null ^ this.getTableName() == null)
            return false;
        if (other.getTableName() != null && other.getTableName().equals(this.getTableName()) == false)
            return false;
        if (other.getUpdateExpression() == null ^ this.getUpdateExpression() == null)
            return false;
        if (other.getUpdateExpression() != null && other.getUpdateExpression().equals(this.getUpdateExpression()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getExpressionAttributeValues() == null) ? 0 : getExpressionAttributeValues().hashCode());
        hashCode = prime * hashCode + ((getKey() == null) ? 0 : getKey().hashCode());
        hashCode = prime * hashCode + ((getReturnValues() == null) ? 0 : getReturnValues().hashCode());
        hashCode = prime * hashCode + ((getTableName() == null) ? 0 : getTableName().hashCode());
        hashCode = prime * hashCode + ((getUpdateExpression() == null) ? 0 : getUpdateExpression().hashCode());
        return hashCode;
    }

    @Override
    public PUTReq clone() {
        try {
            return (PUTReq) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

    @com.amazonaws.annotation.SdkInternalApi
    @Override
    public void marshall(ProtocolMarshaller protocolMarshaller) {
        org.uniroma2.sdcc.Traffic.model.transform.PUTReqMarshaller.getInstance().marshall(this, protocolMarshaller);
    }
}
