/**
 * null
 */
package org.uniroma2.sdcc.Traffic.model.transform;

import java.util.List;
import javax.annotation.Generated;

import com.amazonaws.SdkClientException;
import org.uniroma2.sdcc.Traffic.model.*;

import com.amazonaws.protocol.*;
import com.amazonaws.annotation.SdkInternalApi;

/**
 * GETTrafficResponseMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class GETTrafficResponseMarshaller {

    private static final MarshallingInfo<List> ITEMS_BINDING = MarshallingInfo.builder(MarshallingType.LIST).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName("Items").build();

    private static final GETTrafficResponseMarshaller instance = new GETTrafficResponseMarshaller();

    public static GETTrafficResponseMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(GETTrafficResponse gETTrafficResponse, ProtocolMarshaller protocolMarshaller) {

        if (gETTrafficResponse == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(gETTrafficResponse.getItems(), ITEMS_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
