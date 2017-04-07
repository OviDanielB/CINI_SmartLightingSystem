/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model;

import java.io.Serializable;
import javax.annotation.Generated;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PostStreeTraffic"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PostStreeTrafficRequest extends com.amazonaws.opensdk.BaseRequest implements Serializable, Cloneable {

    private POSTReq pOSTReq;

    /**
     * @param pOSTReq
     */

    public void setPOSTReq(POSTReq pOSTReq) {
        this.pOSTReq = pOSTReq;
    }

    /**
     * @return
     */

    public POSTReq getPOSTReq() {
        return this.pOSTReq;
    }

    /**
     * @param pOSTReq
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public PostStreeTrafficRequest pOSTReq(POSTReq pOSTReq) {
        setPOSTReq(pOSTReq);
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
        if (getPOSTReq() != null)
            sb.append("POSTReq: ").append(getPOSTReq());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof PostStreeTrafficRequest == false)
            return false;
        PostStreeTrafficRequest other = (PostStreeTrafficRequest) obj;
        if (other.getPOSTReq() == null ^ this.getPOSTReq() == null)
            return false;
        if (other.getPOSTReq() != null && other.getPOSTReq().equals(this.getPOSTReq()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getPOSTReq() == null) ? 0 : getPOSTReq().hashCode());
        return hashCode;
    }

    @Override
    public PostStreeTrafficRequest clone() {
        return (PostStreeTrafficRequest) super.clone();
    }

    /**
     * Set the configuration for this request.
     *
     * @param sdkRequestConfig
     *        Request configuration.
     * @return This object for method chaining.
     */
    public PostStreeTrafficRequest sdkRequestConfig(com.amazonaws.opensdk.SdkRequestConfig sdkRequestConfig) {
        super.sdkRequestConfig(sdkRequestConfig);
        return this;
    }

}
