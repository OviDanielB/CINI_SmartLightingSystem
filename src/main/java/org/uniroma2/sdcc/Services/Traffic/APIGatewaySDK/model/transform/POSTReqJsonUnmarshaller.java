/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.POSTReq;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * POSTReq JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class POSTReqJsonUnmarshaller implements Unmarshaller<POSTReq, JsonUnmarshallerContext> {

    public POSTReq unmarshall(JsonUnmarshallerContext context) throws Exception {
        POSTReq pOSTReq = new POSTReq();

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
                if (context.testExpression("Item", targetDepth)) {
                    context.nextToken();
                    pOSTReq.setItem(ItemJsonUnmarshaller.getInstance().unmarshall(context));
                }
                if (context.testExpression("TableName", targetDepth)) {
                    context.nextToken();
                    pOSTReq.setTableName(context.getUnmarshaller(String.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return pOSTReq;
    }

    private static POSTReqJsonUnmarshaller instance;

    public static POSTReqJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new POSTReqJsonUnmarshaller();
        return instance;
    }
}
