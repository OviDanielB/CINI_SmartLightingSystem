package org.uniroma2.sdcc.Traffic;

import org.uniroma2.sdcc.Model.TrafficData;

import java.util.List;

/**
 * Interface for Street traffic congestion retrieval;
 * useful if underlying data source changes, making it more flexible
 */
public interface StreetTraffic {

    /**
     * retrieve all streets in the city with
     * relative traffic congestion percentage
     * @return list of traffic data
     */
    List<TrafficData> getAllCityStreetsTraffic();

    /**
     * insert a new street with relative percentage
     * @param street new
     * @param trafficCongPerc traffic percentage
     * @return true if operation succeded, false otherwise
     */
    boolean insertNewStreet(String street, Float trafficCongPerc);

    /**
     * update street traffic percentage
     * @param street existing
     * @param newTraffic new traffic value
     * @return true if operation succeded, false otherwise
     */
    boolean updateStreetTraffic(String street,Float newTraffic);

    /**
     * delete an street with relative values(traffic,city,etc)
     * @param street existing
     * @return true if operation succeded, false otherwise
     */
    boolean deleteStreet(String street);

    /**
     * close connection with data source
     */
    void closeConn();

}
