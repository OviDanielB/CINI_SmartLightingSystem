/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.Key;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * Key JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class KeyJsonUnmarshaller implements Unmarshaller<Key, JsonUnmarshallerContext> {

    public Key unmarshall(JsonUnmarshallerContext context) throws Exception {
        Key key = new Key();

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
                if (context.testExpression("City", targetDepth)) {
                    context.nextToken();
                    key.setCity(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("Street", targetDepth)) {
                    context.nextToken();
                    key.setStreet(context.getUnmarshaller(String.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return key;
    }

    private static KeyJsonUnmarshaller instance;

    public static KeyJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new KeyJsonUnmarshaller();
        return instance;
    }
}
