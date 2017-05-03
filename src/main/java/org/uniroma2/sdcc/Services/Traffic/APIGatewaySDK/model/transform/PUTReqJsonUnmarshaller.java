/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.PUTReq;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * PUTReq JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PUTReqJsonUnmarshaller implements Unmarshaller<PUTReq, JsonUnmarshallerContext> {

    public PUTReq unmarshall(JsonUnmarshallerContext context) throws Exception {
        PUTReq pUTReq = new PUTReq();

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
                if (context.testExpression("ExpressionAttributeValues", targetDepth)) {
                    context.nextToken();
                    pUTReq.setExpressionAttributeValues(ExpressionAttributeValuesJsonUnmarshaller.getInstance().unmarshall(context));
                }
                if (context.testExpression("Key", targetDepth)) {
                    context.nextToken();
                    pUTReq.setKey(KeyJsonUnmarshaller.getInstance().unmarshall(context));
                }
                if (context.testExpression("ReturnValues", targetDepth)) {
                    context.nextToken();
                    pUTReq.setReturnValues(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("TableName", targetDepth)) {
                    context.nextToken();
                    pUTReq.setTableName(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("UpdateExpression", targetDepth)) {
                    context.nextToken();
                    pUTReq.setUpdateExpression(context.getUnmarshaller(String.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return pUTReq;
    }

    private static PUTReqJsonUnmarshaller instance;

    public static PUTReqJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new PUTReqJsonUnmarshaller();
        return instance;
    }
}
