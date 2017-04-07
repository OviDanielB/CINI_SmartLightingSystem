/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model.transform;

import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import com.amazonaws.Request;

import com.amazonaws.http.HttpMethodName;
import org.uniroma2.sdcc.Traffic.model.*;
import com.amazonaws.transform.Marshaller;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * PutStreeTrafficRequest Marshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class PutStreeTrafficRequestProtocolMarshaller implements Marshaller<Request<PutStreeTrafficRequest>, PutStreeTrafficRequest> {

    private static final OperationInfo SDK_OPERATION_BINDING = OperationInfo.builder().protocol(Protocol.API_GATEWAY).requestUri("/test/StreeTraffic")
            .httpMethodName(HttpMethodName.PUT).hasExplicitPayloadMember(true).hasPayloadMembers(true).serviceName("Traffic").build();

    private final com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl protocolFactory;

    public PutStreeTrafficRequestProtocolMarshaller(com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    public Request<PutStreeTrafficRequest> marshall(PutStreeTrafficRequest putStreeTrafficRequest) {

        if (putStreeTrafficRequest == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            final ProtocolRequestMarshaller<PutStreeTrafficRequest> protocolMarshaller = protocolFactory.createProtocolMarshaller(SDK_OPERATION_BINDING,
                    putStreeTrafficRequest);

            protocolMarshaller.startMarshalling();
            PutStreeTrafficRequestMarshaller.getInstance().marshall(putStreeTrafficRequest, protocolMarshaller);
            return protocolMarshaller.finishMarshalling();
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
