/**
 * null
 */
package org.uniroma2.sdcc.Traffic;

import com.amazonaws.annotation.NotThreadSafe;
import com.amazonaws.client.AwsSyncClientParams;
import com.amazonaws.opensdk.protect.client.SdkSyncClientBuilder;
import com.amazonaws.opensdk.internal.config.ApiGatewayClientConfigurationFactory;
import com.amazonaws.util.RuntimeHttpUtils;
import com.amazonaws.Protocol;

import java.net.URI;
import javax.annotation.Generated;

/**
 * Fluent builder for {@link org.uniroma2.sdcc.Traffic.Traffic}.
 * 
 * @see org.uniroma2.sdcc.Traffic.Traffic#builder
 **/
@NotThreadSafe
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public final class TrafficClientBuilder extends SdkSyncClientBuilder<TrafficClientBuilder, Traffic> {

    private static final URI DEFAULT_ENDPOINT = RuntimeHttpUtils.toUri("gswnejacse.execute-api.eu-west-1.amazonaws.com", Protocol.HTTPS);
    private static final String DEFAULT_REGION = "eu-west-1";

    /**
     * Package private constructor - builder should be created via {@link Traffic#builder()}
     */
    TrafficClientBuilder() {
        super(new ApiGatewayClientConfigurationFactory());
    }

    /**
     * Construct a synchronous implementation of Traffic using the current builder configuration.
     *
     * @param params
     *        Current builder configuration represented as a parameter object.
     * @return Fully configured implementation of Traffic.
     */
    @Override
    protected Traffic build(AwsSyncClientParams params) {
        return new TrafficClient(params);
    }

    @Override
    protected URI defaultEndpoint() {
        return DEFAULT_ENDPOINT;
    }

    @Override
    protected String defaultRegion() {
        return DEFAULT_REGION;
    }

}
