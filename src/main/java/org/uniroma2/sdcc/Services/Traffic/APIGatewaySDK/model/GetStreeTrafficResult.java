/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model;

import java.io.Serializable;
import javax.annotation.Generated;

/**
 * 
 * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/GetStreeTraffic"
 *      target="_top">AWS API Documentation</a>
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class GetStreeTrafficResult extends com.amazonaws.opensdk.BaseResult implements Serializable, Cloneable {

    private GETTrafficResponse gETTrafficResponse;

    /**
     * @param gETTrafficResponse
     */

    public void setGETTrafficResponse(GETTrafficResponse gETTrafficResponse) {
        this.gETTrafficResponse = gETTrafficResponse;
    }

    /**
     * @return
     */

    public GETTrafficResponse getGETTrafficResponse() {
        return this.gETTrafficResponse;
    }

    /**
     * @param gETTrafficResponse
     * @return Returns a reference to this object so that method calls can be chained together.
     */

    public GetStreeTrafficResult gETTrafficResponse(GETTrafficResponse gETTrafficResponse) {
        setGETTrafficResponse(gETTrafficResponse);
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
        if (getGETTrafficResponse() != null)
            sb.append("GETTrafficResponse: ").append(getGETTrafficResponse());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;

        if (obj instanceof GetStreeTrafficResult == false)
            return false;
        GetStreeTrafficResult other = (GetStreeTrafficResult) obj;
        if (other.getGETTrafficResponse() == null ^ this.getGETTrafficResponse() == null)
            return false;
        if (other.getGETTrafficResponse() != null && other.getGETTrafficResponse().equals(this.getGETTrafficResponse()) == false)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((getGETTrafficResponse() == null) ? 0 : getGETTrafficResponse().hashCode());
        return hashCode;
    }

    @Override
    public GetStreeTrafficResult clone() {
        try {
            return (GetStreeTrafficResult) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Got a CloneNotSupportedException from Object.clone() " + "even though we're Cloneable!", e);
        }
    }

}
