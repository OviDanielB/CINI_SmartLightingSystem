/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.*;

/**
 * Interface for accessing Traffic.
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public interface Traffic {

    /**
     * @param deleteStreeTrafficRequest
     * @return Result of the DeleteStreeTraffic operation returned by the service.
     * @sample Traffic.DeleteStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/DeleteStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    DeleteStreeTrafficResult deleteStreeTraffic(DeleteStreeTrafficRequest deleteStreeTrafficRequest);

    /**
     * @param getStreeTrafficRequest
     * @return Result of the GetStreeTraffic operation returned by the service.
     * @sample Traffic.GetStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/GetStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    GetStreeTrafficResult getStreeTraffic(GetStreeTrafficRequest getStreeTrafficRequest);

    /**
     * @param postStreeTrafficRequest
     * @return Result of the PostStreeTraffic operation returned by the service.
     * @sample Traffic.PostStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PostStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    PostStreeTrafficResult postStreeTraffic(PostStreeTrafficRequest postStreeTrafficRequest);

    /**
     * @param putStreeTrafficRequest
     * @return Result of the PutStreeTraffic operation returned by the service.
     * @sample Traffic.PutStreeTraffic
     * @see <a href="http://docs.aws.amazon.com/goto/WebAPI/gswnejacse-2017-04-07T09:59:10Z/PutStreeTraffic"
     *      target="_top">AWS API Documentation</a>
     */
    PutStreeTrafficResult putStreeTraffic(PutStreeTrafficRequest putStreeTrafficRequest);

    /**
     * @return Create new instance of builder with all defaults set.
     */
    public static TrafficClientBuilder builder() {
        return new TrafficClientBuilder();
    }

    /**
     * Shuts down this client object, releasing any resources that might be held open. This is an optional method, and
     * callers are not expected to call it, but can if they want to explicitly release any open resources. Once a client
     * has been shutdown, it should not be used to make any more requests.
     */
    void shutdown();

}
