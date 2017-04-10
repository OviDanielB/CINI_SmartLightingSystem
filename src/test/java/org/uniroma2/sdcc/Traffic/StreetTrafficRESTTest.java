package org.uniroma2.sdcc.Traffic;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.uniroma2.sdcc.Model.TrafficData;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ovidiudanielbarba on 07/04/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StreetTrafficRESTTest {

    private static final String TEST_STREET = "TestStreet";
    private static final String TEST_CITY = "Rome";
    private static final Float TEST_PERC = 89f;
    private static final Float TEST_PERC_UPD = 11f;


    @org.junit.Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning StreetTrafficREST Test");
    }

    @Test
    public void OP1_insertNewStreet() throws Exception {

        System.out.println("INSERTION");

        StreetTraffic traffic = new StreetTrafficREST();

        assertTrue(traffic.insertNewStreet(TEST_STREET,TEST_PERC));

        traffic.closeConn();


    }

    @Test
    public void OP2_updateStreetTraffic() throws Exception {
        System.out.println("UPDATE");

        StreetTraffic traffic = new StreetTrafficREST();

        assertTrue(traffic.updateStreetTraffic(TEST_STREET,TEST_PERC_UPD));

        traffic.closeConn();
    }


    @Test
    public void OP3_getAllCityStreetsTraffic() throws Exception {

        System.out.println("GET");

        Thread.sleep(2000);
        List<TrafficData> trafficDataList;
        StreetTraffic traffic = new StreetTrafficREST();

        trafficDataList = traffic.getAllCityStreetsTraffic();

        /* success if TEST_STREET is present with relative TEST_PERC */
        assertTrue(trafficDataList.stream()
                .filter(e -> e.getStreet().equals(TEST_STREET) && e.getCongestionPercentage().equals(TEST_PERC_UPD))
                .count() > 0);

        traffic.closeConn();
    }


    @Test
    public void OP4_deleteStreet() throws Exception {

        StreetTraffic traffic = new StreetTrafficREST();

        assertTrue(traffic.deleteStreet(TEST_STREET));

        traffic.closeConn();


    }

}