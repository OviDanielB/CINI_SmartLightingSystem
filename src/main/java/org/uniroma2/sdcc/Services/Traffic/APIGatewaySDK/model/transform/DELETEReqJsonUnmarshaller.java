/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.DELETEReq;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * DELETEReq JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class DELETEReqJsonUnmarshaller implements Unmarshaller<DELETEReq, JsonUnmarshallerContext> {

    public DELETEReq unmarshall(JsonUnmarshallerContext context) throws Exception {
        DELETEReq dELETEReq = new DELETEReq();

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
                if (context.testExpression("Key", targetDepth)) {
                    context.nextToken();
                    dELETEReq.setKey(KeyJsonUnmarshaller.getInstance().unmarshall(context));
                }
                if (context.testExpression("TableName", targetDepth)) {
                    context.nextToken();
                    dELETEReq.setTableName(context.getUnmarshaller(String.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return dELETEReq;
    }

    private static DELETEReqJsonUnmarshaller instance;

    public static DELETEReqJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new DELETEReqJsonUnmarshaller();
        return instance;
    }
}
