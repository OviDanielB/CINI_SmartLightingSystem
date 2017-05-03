/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.GETTrafficResponse;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.ItemsItem;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * GETTrafficResponse JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class GETTrafficResponseJsonUnmarshaller implements Unmarshaller<GETTrafficResponse, JsonUnmarshallerContext> {

    public GETTrafficResponse unmarshall(JsonUnmarshallerContext context) throws Exception {
        GETTrafficResponse gETTrafficResponse = new GETTrafficResponse();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL) {
            return null;
        }

        while (true) {
            if (token == null)
                break;

            if (token == FIELD_NAME || token == START_OBJECT) {
                if (context.testExpression("Items", targetDepth)) {
                    context.nextToken();
                    gETTrafficResponse.setItems(new ListUnmarshaller<ItemsItem>(ItemsItemJsonUnmarshaller.getInstance()).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return gETTrafficResponse;
    }

    private static GETTrafficResponseJsonUnmarshaller instance;

    public static GETTrafficResponseJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new GETTrafficResponseJsonUnmarshaller();
        return instance;
    }
}
