package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test PlanBolt operation.
 */
public class PlanBoltTest {

    private Float traffic_tolerance = 20f;
    private Float parking_tolerance = 20f;

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning PlanBolt Test");

    }

    /**
     * Test adaptation according to gap to increase based on anomalies monitoring,
     * traffic and parking percentages.
     */
    @Test
    public void Test1_toIncreaseWithTrafficAndParking() throws Exception {

        Float intensity = 50f;
        Float toIncreaseGap = 10f;
        Float toDecreaseGap = -5f;
        Float traffic = 60f;
        Float parking = 50f;

        Float expected_adapted_intensity =
                ((intensity + toIncreaseGap) + traffic*(100 - (intensity + toIncreaseGap))/100)
                        + parking*(100 - ((intensity + toIncreaseGap) + traffic*(100 - (intensity + toIncreaseGap))/100))/100;

        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);

        assertEquals(expected_adapted_intensity,adapted_intensity);
    }

    /**
     * Test adaptation according to gap to decrease based on anomalies monitoring
     * that is neglected because of relevant traffic and parking percentages.
     */
    @Test
    public void Test2_toDecreaseWithTrafficAndParking() throws Exception {

        Float intensity = 50f;
        Float toIncreaseGap = 0f;
        Float toDecreaseGap = -10f;
        Float traffic = 60f;
        Float parking = 50f;

        Float expected_adapted_intensity = (intensity + traffic*(100 - intensity)/100)
                + parking*(100 - (intensity + traffic*(100 - intensity)/100))/100;

        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);

        assertEquals(expected_adapted_intensity,adapted_intensity);

    }

    /**
     * Test if without relevant traffic and parking percentage intensity is decreased.
    */
    @Test
    public void Test3_toDecreaseWithoutTrafficAndParking() throws Exception {

        Float intensity = 50f;
        Float toIncreaseGap = 0f;
        Float toDecreaseGap = -10f;
        Float traffic = 10f;
        Float parking = 5f;

        Float expected_adapted_intensity = intensity + toDecreaseGap;

        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);

        assertEquals(expected_adapted_intensity,adapted_intensity);

    }

    /**
     * Test if without relevant traffic and parking percentage increasing gap has higher
     * priority that the decreasing one.
     */
    @Test
    public void Test4_toIncreaseInsteadOfDecreaseWithoutTrafficAndParking() throws Exception {

        Float intensity = 50f;
        Float toIncreaseGap = 5f;
        Float toDecreaseGap = -10f;
        Float traffic = 10f;
        Float parking = 5f;

        Float expected_adapted_intensity = intensity + toIncreaseGap;

        Float adapted_intensity = adaptByTrafficLevelAndAnomalies(toIncreaseGap,toDecreaseGap,intensity,traffic, parking);

        assertEquals(expected_adapted_intensity,adapted_intensity);

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended PlanBolt Test");
    }

    private Float adaptByTrafficLevelAndAnomalies(Float toIncreaseGap, Float toDecreaseGap,
                                                 Float current_intensity, Float traffic,
                                                 Float parking) {

        Float adapted_intensity;
        /*
         *  If there are both positive and negative luminosity gaps
         *  of distance from correct value, the positive one is preferred
         */
        if (toIncreaseGap.equals(0f)) {

            adapted_intensity = current_intensity;

            // traffic level relevant just above a threshold
            if ((traffic - 0f) > traffic_tolerance) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic*(100 - adapted_intensity)/100;
            }
            if ((parking - 0f) > parking_tolerance) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking*(100 - adapted_intensity)/100;
            } else {
                // if no positive gap measured and no relevant traffic or parking percentages
                // the negative gap is considered
                adapted_intensity = current_intensity + toDecreaseGap;
            }
        } else {

            adapted_intensity = current_intensity + toIncreaseGap;

            // traffic level relevant just above a threshold
            if ((traffic - 0f) > traffic_tolerance) {
                // to increase intensity of traffic level percentage of current adapted intensity
                adapted_intensity = adapted_intensity + traffic*(100 - adapted_intensity)/100;
            }
            if ((parking - 0f) > parking_tolerance) { // parking availability relevant just above a threshold
                // to increase intensity of parking occupation percentage of current adapted intensity
                adapted_intensity = adapted_intensity + parking*(100 - adapted_intensity)/100;
            }
        }
        return adapted_intensity;
    }

}