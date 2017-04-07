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
 * PostStreeTrafficResult JSON Unmarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
public class PostStreeTrafficResultJsonUnmarshaller implements Unmarshaller<PostStreeTrafficResult, JsonUnmarshallerContext> {

    public PostStreeTrafficResult unmarshall(JsonUnmarshallerContext context) throws Exception {
        PostStreeTrafficResult postStreeTrafficResult = new PostStreeTrafficResult();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL) {
            return postStreeTrafficResult;
        }

        while (true) {
            if (token == null)
                break;

            postStreeTrafficResult.setEmpty(EmptyJsonUnmarshaller.getInstance().unmarshall(context));
            token = context.nextToken();
        }

        return postStreeTrafficResult;
    }

    private static PostStreeTrafficResultJsonUnmarshaller instance;

    public static PostStreeTrafficResultJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new PostStreeTrafficResultJsonUnmarshaller();
        return instance;
    }
}
