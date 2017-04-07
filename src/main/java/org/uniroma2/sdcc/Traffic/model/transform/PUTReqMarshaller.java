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
 * PUTReqMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class PUTReqMarshaller {

    private static final MarshallingInfo<StructuredPojo> EXPRESSIONATTRIBUTEVALUES_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("ExpressionAttributeValues").build();
    private static final MarshallingInfo<StructuredPojo> KEY_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("Key").build();
    private static final MarshallingInfo<String> RETURNVALUES_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("ReturnValues").build();
    private static final MarshallingInfo<String> TABLENAME_BINDING = MarshallingInfo.builder(MarshallingType.STRING).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName("TableName").build();
    private static final MarshallingInfo<String> UPDATEEXPRESSION_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("UpdateExpression").build();

    private static final PUTReqMarshaller instance = new PUTReqMarshaller();

    public static PUTReqMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(PUTReq pUTReq, ProtocolMarshaller protocolMarshaller) {

        if (pUTReq == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(pUTReq.getExpressionAttributeValues(), EXPRESSIONATTRIBUTEVALUES_BINDING);
            protocolMarshaller.marshall(pUTReq.getKey(), KEY_BINDING);
            protocolMarshaller.marshall(pUTReq.getReturnValues(), RETURNVALUES_BINDING);
            protocolMarshaller.marshall(pUTReq.getTableName(), TABLENAME_BINDING);
            protocolMarshaller.marshall(pUTReq.getUpdateExpression(), UPDATEEXPRESSION_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
