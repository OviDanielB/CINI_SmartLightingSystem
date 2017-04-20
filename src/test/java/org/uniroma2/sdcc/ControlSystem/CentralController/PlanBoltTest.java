package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.Traffic;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test PlanBolt operation.
 */

/**
 * Test Execute Bolt for main functionalities
 */
public class PlanBoltTest {

    private PlanBolt bolt;
    private Gson gson;

    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector outputCollector;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        gson = new Gson();

        bolt = new PlanBolt();
        bolt.prepare(new Config(),topologyContext,outputCollector);
    }

    /**
     * test the main execute method;
     * functionalities tested elsewhere (SNSWriterTest)
     * @throws Exception
     */
    @Test
    public void execute() throws Exception {

        TrafficData trafficData = mock(TrafficData.class);
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(0f);

        ParkingData parkingData = mock(ParkingData.class);
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(0f);

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(20f);
        when(tuple.getFloatByField(Constants.GAP_TO_INCREASE)).thenReturn(10f);
        when(tuple.getFloatByField(Constants.GAP_TO_DECREASE)).thenReturn(-2f);
        when(tuple.getValueByField(Constants.TRAFFIC_BY_ADDRESS)).thenReturn(gson.toJson(trafficData));
        when(tuple.getValueByField(Constants.PARKING_BY_CELLID)).thenReturn(gson.toJson(parkingData));

        bolt.execute(tuple);
    }

}
//public class PlanBoltTest {
//
//    @Before
//    public void setUp() throws Exception {
//        System.out.println("[CINI] [TEST] Beginning PlanBolt Test");
//
//    }
//
//    /**
//     * Test adaptation according to gap to increase based on anomalies monitoring,
//     * traffic and parking percentages.
//     */
//    @Test
//    public void Test1_toIncreaseWithTrafficAndParking() throws Exception {
//
//        Float intensity = 50f;
//        Float toIncreaseGap = 10f;
//        Float toDecreaseGap = -5f;
//        Float traffic = 60f;
//        Float parking = 50f;
//
//        Float expected_adapted_intensity =
//                ((intensity + toIncreaseGap) + traffic*(100 - (intensity + toIncreaseGap))/100)
//                        + parking*(100 - ((intensity + toIncreaseGap) + traffic*(100 - (intensity + toIncreaseGap))/100))/100;
//
//        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);
//
//        assertEquals(expected_adapted_intensity,adapted_intensity);
//    }
//
//    /**
//     * Test adaptation according to gap to decrease based on anomalies monitoring
//     * that is neglected because of relevant traffic and parking percentages.
//     */
//    @Test
//    public void Test2_toDecreaseWithTrafficAndParking() throws Exception {
//
//        Float intensity = 50f;
//        Float toIncreaseGap = 0f;
//        Float toDecreaseGap = -10f;
//        Float traffic = 60f;
//        Float parking = 50f;
//
//        Float expected_adapted_intensity = (intensity + traffic*(100 - intensity)/100)
//                + parking*(100 - (intensity + traffic*(100 - intensity)/100))/100;
//
//        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);
//
//        assertEquals(expected_adapted_intensity,adapted_intensity);
//
//    }
//
//    /**
//     * Test if without relevant traffic and parking percentage increasing gap has higher
//     * priority that the decreasing one.
//     */
//    @Test
//    public void Test4_toIncreaseInsteadOfDecreaseWithoutTrafficAndParking() throws Exception {
//
//        Float intensity = 50f;
//        Float toIncreaseGap = 5f;
//        Float toDecreaseGap = -10f;
//        Float traffic = 10f;
//        Float parking = 5f;
//
//        Float expected_adapted_intensity = ((intensity+toIncreaseGap) + traffic*(100 - (intensity + toIncreaseGap))/100)
//                + parking*(100 - ((intensity + toIncreaseGap) + traffic*(100 - (intensity+toIncreaseGap))/100))/100;
//
//        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);
//
//        assertEquals(expected_adapted_intensity,adapted_intensity);
//
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        System.out.println("[CINI] [TEST] Ended PlanBolt Test");
//    }
//
//    private Float adaptByTrafficLevelAndAnomalies(Float toIncreaseGap, Float toDecreaseGap,
//                                                 Float current_intensity, Float traffic,
//                                                 Float parking) {
//
//        Float adapted_intensity;
//
//        /*
//         *  If there are both positive and negative luminosity gaps
//         *  of distance from correct value, the positive one is preferred
//         */
//        if (toIncreaseGap.equals(0f)) {
//
//            adapted_intensity = current_intensity;
//
//            if (!traffic.equals(0f)) {
//                // to increase intensity of traffic level percentage of current adapted intensity
//                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
//            }
//            if (!parking.equals(0f)) {
//                // to increase intensity of parking occupation percentage of current adapted intensity
//                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
//            } else {
//                // if no positive gap measured and no relevant traffic or parking percentages
//                // the negative gap is considered
//                adapted_intensity = current_intensity + toDecreaseGap;
//            }
//        } else {
//
//            adapted_intensity = current_intensity + toIncreaseGap;
//            // traffic level relevant just above a threshold
//            if (!traffic.equals(0f)) {
//                // to increase intensity of traffic level percentage of current adapted intensity
//                adapted_intensity = adapted_intensity + traffic * (100 - adapted_intensity)/100;
//            }
//            if (!parking.equals(0f)) { // parking availability relevant just above a threshold
//                // to increase intensity of parking occupation percentage of current adapted intensity
//                adapted_intensity = adapted_intensity + parking * (100 - adapted_intensity)/100;
//            }
//        }
//        return adapted_intensity;
//    }
//}