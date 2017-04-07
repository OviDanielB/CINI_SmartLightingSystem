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
 * ItemMarshaller
 */
@Generated("com.amazonaws:aws-java-sdk-code-generator")
@SdkInternalApi
public class ItemMarshaller {

    private static final MarshallingInfo<String> CITY_BINDING = MarshallingInfo.builder(MarshallingType.STRING).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName("City").build();
    private static final MarshallingInfo<String> STREET_BINDING = MarshallingInfo.builder(MarshallingType.STRING).marshallLocation(MarshallLocation.PAYLOAD)
            .marshallLocationName("Street").build();
    private static final MarshallingInfo<Double> TRAFFICPERC_BINDING = MarshallingInfo.builder(MarshallingType.DOUBLE)
            .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("TrafficPerc").build();

    private static final ItemMarshaller instance = new ItemMarshaller();

    public static ItemMarshaller getInstance() {
        return instance;
    }

    /**
     * Marshall the given parameter object.
     */
    public void marshall(Item item, ProtocolMarshaller protocolMarshaller) {

        if (item == null) {
            throw new SdkClientException("Invalid argument passed to marshall(...)");
        }

        try {
            protocolMarshaller.marshall(item.getCity(), CITY_BINDING);
            protocolMarshaller.marshall(item.getStreet(), STREET_BINDING);
            protocolMarshaller.marshall(item.getTrafficPerc(), TRAFFICPERC_BINDING);
        } catch (Exception e) {
            throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
        }
    }

}
