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
 * DeleteStreeTrafficResult JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class DeleteStreeTrafficResultJsonUnmarshaller implements Unmarshaller<DeleteStreeTrafficResult, JsonUnmarshallerContext> {

    public DeleteStreeTrafficResult unmarshall(JsonUnmarshallerContext context) throws Exception {
        DeleteStreeTrafficResult deleteStreeTrafficResult = new DeleteStreeTrafficResult();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL) {
            return deleteStreeTrafficResult;
        }

        while (true) {
            if (token == null)
                break;

            deleteStreeTrafficResult.setEmpty(EmptyJsonUnmarshaller.getInstance().unmarshall(context));
            token = context.nextToken();
        }

        return deleteStreeTrafficResult;
    }

    private static DeleteStreeTrafficResultJsonUnmarshaller instance;

    public static DeleteStreeTrafficResultJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new DeleteStreeTrafficResultJsonUnmarshaller();
        return instance;
    }
}
