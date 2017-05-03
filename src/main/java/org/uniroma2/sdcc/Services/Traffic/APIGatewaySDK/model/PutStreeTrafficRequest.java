/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model;

import java.io.Serializable;
import javax.annotation.Generated;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PutStreeTraffic"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PutStreeTrafficRequest extends com.amazonaws.opensdk.BaseRequest implements Serializable, Cloneable {

    private PUTReq pUTReq;

    /**
     * @param pUTReq
     */

    public void setPUTReq(PUTReq pUTReq) {
        this.pUTReq = pUTReq;
    }

    /**
     * @return
     */

    public PUTReq getPUTReq() {
        return this.pUTReq;
    }

    /**
     * @param pUTReq
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public PutStreeTrafficRequest pUTReq(PUTReq pUTReq) {
        setPUTReq(pUTReq);
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
        if (getPUTReq() != null)
            sb.append("PUTReq: ").append(getPUTReq());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof PutStreeTrafficRequest == false)
            return false;
        PutStreeTrafficRequest other = (PutStreeTrafficRequest) obj;
        if (other.getPUTReq() == null ^ this.getPUTReq() == null)
            return false;
        if (other.getPUTReq() != null && other.getPUTReq().equals(this.getPUTReq()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getPUTReq() == null) ? 0 : getPUTReq().hashCode());
        return hashCode;
    }

    @Override
    public PutStreeTrafficRequest clone() {
        return (PutStreeTrafficRequest) super.clone();
    }

    /**
     * Set the configuration for this request.
     *
     * @param sdkRequestConfig
     *        Request configuration.
     * @return This object for method chaining.
     */
    public PutStreeTrafficRequest sdkRequestConfig(com.amazonaws.opensdk.SdkRequestConfig sdkRequestConfig) {
        super.sdkRequestConfig(sdkRequestConfig);
        return this;
    }

}
