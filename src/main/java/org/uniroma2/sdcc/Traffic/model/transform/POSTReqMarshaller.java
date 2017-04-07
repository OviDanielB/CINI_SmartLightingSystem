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
 * POSTReqMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class POSTReqMarshaller {

    private static final MarshallingInfo<StructuredPojo> ITEM_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("Item").build();
    private static final MarshallingInfo<String> TABLENAME_BINDING = MarshallingInfo.builder(MarshallingType.STRING).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName("TableName").build();

    private static final POSTReqMarshaller instance = new POSTReqMarshaller();

    public static POSTReqMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(POSTReq pOSTReq, ProtocolMarshaller protocolMarshaller) {

        if (pOSTReq == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(pOSTReq.getItem(), ITEM_BINDING);
            protocolMarshaller.marshall(pOSTReq.getTableName(), TABLENAME_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
