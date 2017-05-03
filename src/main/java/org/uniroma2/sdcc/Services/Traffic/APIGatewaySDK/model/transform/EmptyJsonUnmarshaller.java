/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.Empty;
import com.amazonaws.transform.*;

/**
 * Empty JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class EmptyJsonUnmarshaller implements Unmarshaller<Empty, JsonUnmarshallerContext> {

    public Empty unmarshall(JsonUnmarshallerContext context) throws Exception {
        Empty empty = new Empty();

        return empty;
    }

    private static EmptyJsonUnmarshaller instance;

    public static EmptyJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new EmptyJsonUnmarshaller();
        return instance;
    }
}
