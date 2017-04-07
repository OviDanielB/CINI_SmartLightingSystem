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
 * Item JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class ItemJsonUnmarshaller implements Unmarshaller<Item, JsonUnmarshallerContext> {

    public Item unmarshall(JsonUnmarshallerContext context) throws Exception {
        Item item = new Item();

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
                    item.setCity(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("Street", targetDepth)) {
                    context.nextToken();
                    item.setStreet(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("TrafficPerc", targetDepth)) {
                    context.nextToken();
                    item.setTrafficPerc(context.getUnmarshaller(Double.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return item;
    }

    private static ItemJsonUnmarshaller instance;

    public static ItemJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new ItemJsonUnmarshaller();
        return instance;
    }
}
