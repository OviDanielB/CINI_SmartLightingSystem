package org.uniroma2.sdcc.Traffic;

import org.uniroma2.sdcc.Model.TrafficData;

import java.util.List;

/**
 * Created by ovidiudanielbarba on 07/04/2017.
 */
public interface StreetTraffic {

    List<TrafficData> getAllCityStreetsTraffic();
    void insertNewStreet(String street, Float trafficCongPerc);
    void updateStreetTraffic(String street,Float newTraffic);
    void deleteStreet(String street);

}
