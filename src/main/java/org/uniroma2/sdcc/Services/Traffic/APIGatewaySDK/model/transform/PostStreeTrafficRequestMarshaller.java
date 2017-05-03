/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.PostStreeTrafficRequest;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * PostStreeTrafficRequestMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class PostStreeTrafficRequestMarshaller {

    private static final MarshallingInfo<StructuredPojo> POSTREQ_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
            .marshallLocation(MarshallLocation.PAYLOAD).isExplicitPayloadMember(true).build();

    private static final PostStreeTrafficRequestMarshaller instance = new PostStreeTrafficRequestMarshaller();

    public static PostStreeTrafficRequestMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(PostStreeTrafficRequest postStreeTrafficRequest, ProtocolMarshaller protocolMarshaller) {

        if (postStreeTrafficRequest == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(postStreeTrafficRequest.getPOSTReq(), POSTREQ_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
