/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.GetStreeTrafficResult;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * GetStreeTrafficResult JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class GetStreeTrafficResultJsonUnmarshaller implements Unmarshaller<GetStreeTrafficResult, JsonUnmarshallerContext> {

    public GetStreeTrafficResult unmarshall(JsonUnmarshallerContext context) throws Exception {
        GetStreeTrafficResult getStreeTrafficResult = new GetStreeTrafficResult();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL) {
            return getStreeTrafficResult;
        }

        while (true) {
            if (token == null)
                break;

            getStreeTrafficResult.setGETTrafficResponse(GETTrafficResponseJsonUnmarshaller.getInstance().unmarshall(context));
            token = context.nextToken();
        }

        return getStreeTrafficResult;
    }

    private static GetStreeTrafficResultJsonUnmarshaller instance;

    public static GetStreeTrafficResultJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new GetStreeTrafficResultJsonUnmarshaller();
        return instance;
    }
}
