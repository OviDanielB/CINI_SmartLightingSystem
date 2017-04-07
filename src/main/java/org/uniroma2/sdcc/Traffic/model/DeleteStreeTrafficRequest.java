/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model;

import java.io.Serializable;
import javax.annotation.Generated;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/DeleteStreeTraffic"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class DeleteStreeTrafficRequest extends com.amazonaws.opensdk.BaseRequest implements Serializable, Cloneable {

    private DELETEReq dELETEReq;

    /**
     * @param dELETEReq
     */

    public void setDELETEReq(DELETEReq dELETEReq) {
        this.dELETEReq = dELETEReq;
    }

    /**
     * @return
     */

    public DELETEReq getDELETEReq() {
        return this.dELETEReq;
    }

    /**
     * @param dELETEReq
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public DeleteStreeTrafficRequest dELETEReq(DELETEReq dELETEReq) {
        setDELETEReq(dELETEReq);
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
        if (getDELETEReq() != null)
            sb.append("DELETEReq: ").append(getDELETEReq());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof DeleteStreeTrafficRequest == false)
            return false;
        DeleteStreeTrafficRequest other = (DeleteStreeTrafficRequest) obj;
        if (other.getDELETEReq() == null ^ this.getDELETEReq() == null)
            return false;
        if (other.getDELETEReq() != null && other.getDELETEReq().equals(this.getDELETEReq()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getDELETEReq() == null) ? 0 : getDELETEReq().hashCode());
        return hashCode;
    }

    @Override
    public DeleteStreeTrafficRequest clone() {
        return (DeleteStreeTrafficRequest) super.clone();
    }

    /**
     * Set the configuration for this request.
     *
     * @param sdkRequestConfig
     *        Request configuration.
     * @return This object for method chaining.
     */
    public DeleteStreeTrafficRequest sdkRequestConfig(com.amazonaws.opensdk.SdkRequestConfig sdkRequestConfig) {
        super.sdkRequestConfig(sdkRequestConfig);
        return this;
    }

}
