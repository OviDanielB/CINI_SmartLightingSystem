/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model.transform;

import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import org.uniroma2.sdcc.Traffic.model.*;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * ExpressionAttributeValuesMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class ExpressionAttributeValuesMarshaller {

    private static final MarshallingInfo<Double> R_BINDING = MarshallingInfo.builder(MarshallingType.DOUBLE).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName(":r").build();

    private static final ExpressionAttributeValuesMarshaller instance = new ExpressionAttributeValuesMarshaller();

    public static ExpressionAttributeValuesMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(ExpressionAttributeValues expressionAttributeValues, ProtocolMarshaller protocolMarshaller) {

        if (expressionAttributeValues == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(expressionAttributeValues.getR(), R_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
