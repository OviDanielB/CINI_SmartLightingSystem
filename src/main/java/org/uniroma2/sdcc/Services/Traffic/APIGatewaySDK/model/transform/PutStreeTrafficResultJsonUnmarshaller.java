/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.PutStreeTrafficResult;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * PutStreeTrafficResult JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PutStreeTrafficResultJsonUnmarshaller implements Unmarshaller<PutStreeTrafficResult, JsonUnmarshallerContext> {

    public PutStreeTrafficResult unmarshall(JsonUnmarshallerContext context) throws Exception {
        PutStreeTrafficResult putStreeTrafficResult = new PutStreeTrafficResult();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL) {
            return putStreeTrafficResult;
        }

        while (true) {
            if (token == null)
                break;

            putStreeTrafficResult.setEmpty(EmptyJsonUnmarshaller.getInstance().unmarshall(context));
            token = context.nextToken();
        }

        return putStreeTrafficResult;
    }

    private static PutStreeTrafficResultJsonUnmarshaller instance;

    public static PutStreeTrafficResultJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new PutStreeTrafficResultJsonUnmarshaller();
        return instance;
    }
}
