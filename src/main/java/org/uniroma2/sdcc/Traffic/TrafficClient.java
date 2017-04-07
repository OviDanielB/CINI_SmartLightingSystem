/**
 * null
 */
package org.uniroma2.sdcc.Traffic;

import java.net.*;
import java.util.*;

import javax.annotation.Generated;

import org.apache.commons.logging.*;

import com.amazonaws.*;
import com.amazonaws.opensdk.*;
import com.amazonaws.opensdk.model.*;
import com.amazonaws.opensdk.protect.model.transform.*;
import com.amazonaws.auth.*;
import com.amazonaws.handlers.*;
import com.amazonaws.http.*;
import com.amazonaws.internal.*;
import com.amazonaws.metrics.*;
import com.amazonaws.regions.*;
import com.amazonaws.transform.*;
import com.amazonaws.util.*;
import com.amazonaws.protocol.json.*;

import com.amazonaws.annotation.ThreadSafe;
import com.amazonaws.client.AwsSyncClientParams;

import com.amazonaws.client.ClientHandler;
import com.amazonaws.client.ClientHandlerParams;
import com.amazonaws.client.ClientExecutionParams;
import com.amazonaws.opensdk.protect.client.SdkClientHandler;
import com.amazonaws.SdkBaseException;

import org.uniroma2.sdcc.Traffic.model.*;
import org.uniroma2.sdcc.Traffic.model.transform.*;

/**
 * Client for accessing Traffic. All service calls made using this client are blocking, and will not return until the
 * service call completes.
 * <p>
 * 
 */
@ThreadSafe
@Generated("com.amazonaws:aws-java-sdk-code-generator")
class TrafficClient implements Traffic {

    private final ClientHandler clientHandler;

    private final com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl protocolFactory = new com.amazonaws.opensdk.protect.protocol.ApiGatewayProtocolFactoryImpl(
            new JsonClientMetadata().withProtocolVersion("1.1").withSupportsCbor(false).withSupportsIon(false).withContentTypeOverride("application/json")
                    .withBaseServiceExceptionClass(org.uniroma2.sdcc.Traffic.model.TrafficException.class));

    /**
     * Constructs a new client to invoke service methods on Traffic using the specified parameters.
     *
     * <p>
     * All service calls made using this new client object are blocking, and will not return until the service call
     * completes.
     *
     * @param clientParams
     *        Object providing client parameters.
     */
    TrafficClient(AwsSyncClientParams clientParams) {
        this.clientHandler = new SdkClientHandler(new ClientHandlerParams().withClientParams(clientParams));
    }

    /**
     * @param deleteStreeTrafficRequest
     * @return Result of the DeleteStreeTraffic operation returned by the service.
     * @sample Traffic.DeleteStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/DeleteStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    @Override
    public DeleteStreeTrafficResult deleteStreeTraffic(DeleteStreeTrafficRequest deleteStreeTrafficRequest) {
        HttpResponseHandler<DeleteStreeTrafficResult> responseHandler = protocolFactory.createResponseHandler(new JsonOperationMetadata().withPayloadJson(true)
                .withHasStreamingSuccessResponse(false), new DeleteStreeTrafficResultJsonUnmarshaller());

        HttpResponseHandler<SdkBaseException> errorResponseHandler = createErrorResponseHandler();

        return clientHandler.execute(new ClientExecutionParams<DeleteStreeTrafficRequest, DeleteStreeTrafficResult>()
                .withMarshaller(new DeleteStreeTrafficRequestProtocolMarshaller(protocolFactory)).withResponseHandler(responseHandler)
                .withErrorResponseHandler(errorResponseHandler).withInput(deleteStreeTrafficRequest));
    }

    /**
     * @param getStreeTrafficRequest
     * @return Result of the GetStreeTraffic operation returned by the service.
     * @sample Traffic.GetStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/GetStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    @Override
    public GetStreeTrafficResult getStreeTraffic(GetStreeTrafficRequest getStreeTrafficRequest) {
        HttpResponseHandler<GetStreeTrafficResult> responseHandler = protocolFactory.createResponseHandler(new JsonOperationMetadata().withPayloadJson(true)
                .withHasStreamingSuccessResponse(false), new GetStreeTrafficResultJsonUnmarshaller());

        HttpResponseHandler<SdkBaseException> errorResponseHandler = createErrorResponseHandler();

        return clientHandler.execute(new ClientExecutionParams<GetStreeTrafficRequest, GetStreeTrafficResult>()
                .withMarshaller(new GetStreeTrafficRequestProtocolMarshaller(protocolFactory)).withResponseHandler(responseHandler)
                .withErrorResponseHandler(errorResponseHandler).withInput(getStreeTrafficRequest));
    }

    /**
     * @param postStreeTrafficRequest
     * @return Result of the PostStreeTraffic operation returned by the service.
     * @sample Traffic.PostStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PostStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    @Override
    public PostStreeTrafficResult postStreeTraffic(PostStreeTrafficRequest postStreeTrafficRequest) {
        HttpResponseHandler<PostStreeTrafficResult> responseHandler = protocolFactory.createResponseHandler(new JsonOperationMetadata().withPayloadJson(true)
                .withHasStreamingSuccessResponse(false), new PostStreeTrafficResultJsonUnmarshaller());

        HttpResponseHandler<SdkBaseException> errorResponseHandler = createErrorResponseHandler();

        return clientHandler.execute(new ClientExecutionParams<PostStreeTrafficRequest, PostStreeTrafficResult>()
                .withMarshaller(new PostStreeTrafficRequestProtocolMarshaller(protocolFactory)).withResponseHandler(responseHandler)
                .withErrorResponseHandler(errorResponseHandler).withInput(postStreeTrafficRequest));
    }

    /**
     * @param putStreeTrafficRequest
     * @return Result of the PutStreeTraffic operation returned by the service.
     * @sample Traffic.PutStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PutStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    @Override
    public PutStreeTrafficResult putStreeTraffic(PutStreeTrafficRequest putStreeTrafficRequest) {
        HttpResponseHandler<PutStreeTrafficResult> responseHandler = protocolFactory.createResponseHandler(new JsonOperationMetadata().withPayloadJson(true)
                .withHasStreamingSuccessResponse(false), new PutStreeTrafficResultJsonUnmarshaller());

        HttpResponseHandler<SdkBaseException> errorResponseHandler = createErrorResponseHandler();

        return clientHandler.execute(new ClientExecutionParams<PutStreeTrafficRequest, PutStreeTrafficResult>()
                .withMarshaller(new PutStreeTrafficRequestProtocolMarshaller(protocolFactory)).withResponseHandler(responseHandler)
                .withErrorResponseHandler(errorResponseHandler).withInput(putStreeTrafficRequest));
    }

    /**
     * Create the error response handler for the operation.
     * 
     * @param errorShapeMetadata
     *        Error metadata for the given operation
     * @return Configured error response handler to pass to HTTP layer
     */
    private HttpResponseHandler<SdkBaseException> createErrorResponseHandler(JsonErrorShapeMetadata... errorShapeMetadata) {
        return protocolFactory.createErrorResponseHandler(new JsonErrorResponseMetadata().withErrorShapes(Arrays.asList(errorShapeMetadata)));
    }

    @Override
    public void shutdown() {
        clientHandler.shutdown();
    }

}
