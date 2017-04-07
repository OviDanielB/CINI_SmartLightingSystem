/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model.transform;

import java.math.*;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Traffic.model.*;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.*;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * ExpressionAttributeValues JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class ExpressionAttributeValuesJsonUnmarshaller implements Unmarshaller<ExpressionAttributeValues, JsonUnmarshallerContext> {

    public ExpressionAttributeValues unmarshall(JsonUnmarshallerContext context) throws Exception {
        ExpressionAttributeValues expressionAttributeValues = new ExpressionAttributeValues();

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
                if (context.testExpression(":r", targetDepth)) {
                    context.nextToken();
                    expressionAttributeValues.setR(context.getUnmarshaller(Double.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return expressionAttributeValues;
    }

    private static ExpressionAttributeValuesJsonUnmarshaller instance;

    public static ExpressionAttributeValuesJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new ExpressionAttributeValuesJsonUnmarshaller();
        return instance;
    }
}
