/**
 * null
 */
package org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.transform;

import javax.annotation.Generated;

import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.ItemsItem;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * ItemsItem JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class ItemsItemJsonUnmarshaller implements Unmarshaller<ItemsItem, JsonUnmarshallerContext> {

    public ItemsItem unmarshall(JsonUnmarshallerContext context) throws Exception {
        ItemsItem itemsItem = new ItemsItem();

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
                    itemsItem.setCity(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("Street", targetDepth)) {
                    context.nextToken();
                    itemsItem.setStreet(context.getUnmarshaller(String.class).unmarshall(context));
                }
                if (context.testExpression("TrafficPerc", targetDepth)) {
                    context.nextToken();
                    itemsItem.setTrafficPerc(context.getUnmarshaller(Double.class).unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return itemsItem;
    }

    private static ItemsItemJsonUnmarshaller instance;

    public static ItemsItemJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new ItemsItemJsonUnmarshaller();
        return instance;
    }
}
