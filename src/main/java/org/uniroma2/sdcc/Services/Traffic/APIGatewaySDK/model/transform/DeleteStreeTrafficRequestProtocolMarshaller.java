/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import com.amazonaws.Request;

import com.amazonaws.http.HttpMethodName;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.DeleteStreeTrafficRequest;
import com.amazonaws.transform.Marshaller;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * DeleteStreeTrafficRequest Marshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class DeleteStreeTrafficRequestProtocolMarshaller implements Marshaller<Request<DeleteStreeTrafficRequest>, DeleteStreeTrafficRequest> {

    private static final OperationInfo SDK_OPERATION_BINDING = OperationInfo.builder().protocol(Protocol.API_GATEWAY).requestUri("/test/StreeTraffic")
            .httpMethodName(HttpMethodName.DELETE).hasExplicitPayloadMember(true).hasPayloadMembers(true).serviceName("Traffic").build();

    private final com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl protocolFactory;

    public DeleteStreeTrafficRequestProtocolMarshaller(com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    public Request<DeleteStreeTrafficRequest> marshall(DeleteStreeTrafficRequest deleteStreeTrafficRequest) {

        if (deleteStreeTrafficRequest == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            final ProtocolRequestMarshaller<DeleteStreeTrafficRequest> protocolMarshaller = protocolFactory.createProtocolMarshaller(SDK_OPERATION_BINDING,
                    deleteStreeTrafficRequest);

            protocolMarshaller.startMarshalling();
            DeleteStreeTrafficRequestMarshaller.getInstance().marshall(deleteStreeTrafficRequest, protocolMarshaller);
            return protocolMarshaller.finishMarshalling();
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
