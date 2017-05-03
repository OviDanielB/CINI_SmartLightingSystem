/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.PutStreeTrafficRequest;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * PutStreeTrafficRequestMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class PutStreeTrafficRequestMarshaller {

    private static final MarshallingInfo<StructuredPojo> PUTREQ_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
            .marshallLocation(MarshallLocation.PAYLOAD).isExplicitPayloadMember(true).build();

    private static final PutStreeTrafficRequestMarshaller instance = new PutStreeTrafficRequestMarshaller();

    public static PutStreeTrafficRequestMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(PutStreeTrafficRequest putStreeTrafficRequest, ProtocolMarshaller protocolMarshaller) {

        if (putStreeTrafficRequest == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(putStreeTrafficRequest.getPUTReq(), PUTREQ_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
