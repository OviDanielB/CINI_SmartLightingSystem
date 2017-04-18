package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Model.MalfunctionType;
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.TrafficData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test AnalyzeBolt operation.
 */
public class AnalyzeBoltTest {

    private Float traffic_tolerance = 20f;
    private Float parking_tolerance = 20f;


    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning AnalyzeBolt Test");
    }

    /**
     * Test if without anomalies no gap to adapt is computed.
     */
    @Test
    public void Test1_noAnomalies() throws Exception {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.NONE,1f);

        TrafficData trafficData = new TrafficData("Via Cambridge", 0f);
        ParkingData parkingData = new ParkingData(1111, "Via Cambridge", 0f);

        Float expected_toIncrease = 0f;
        Float expected_toDecrease = 0f;

        List<Float> gap = computeAdaptationGaps(anomalies, trafficData, parkingData);

        assertEquals(gap.get(0), expected_toIncrease);
        assertEquals(gap.get(1), expected_toDecrease);
    }

    /**
     * Test if lamp damaged no gap to adapt is computed, even if
     * can have some anomalies.
     */
    @Test
    public void Test2_lampDamaged() throws Exception {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.DAMAGED_BULB,1f);
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE,10f);

        TrafficData trafficData = new TrafficData("Via Cambridge", 0f);
        ParkingData parkingData = new ParkingData(1111, "Via Cambridge", 0f);

        Float expected_toIncrease = 0f;
        Float expected_toDecrease = 0f;

        List<Float> gap = computeAdaptationGaps(anomalies, trafficData, parkingData);

        assertEquals(gap.get(0), expected_toIncrease);
        assertEquals(gap.get(1), expected_toDecrease);
    }

    /**
     * Test if no change is required if no gap to adapt is computed and no
     * relevant traffic congestion and parking occupation is measured.
     */
    @Test
    public void Test3_noChangeRequired() throws Exception {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.NONE,1f);

        TrafficData trafficData = new TrafficData("Via Cambridge", 10f);
        ParkingData parkingData = new ParkingData(1111, "Via Cambridge", 10f);

        Float expected_toIncrease = 0f;
        Float expected_toDecrease = 0f;

        List<Float> gap = computeAdaptationGaps(anomalies, trafficData, parkingData);

        assertEquals(gap.get(0), expected_toIncrease);
        assertEquals(gap.get(1), expected_toDecrease);
    }

    /**
     * Test if change is required if no gap to adapt is computed but
     * relevant traffic congestion and parking occupation are measured.
     */
    @Test
    public void Test3_changeRequired() throws Exception {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS,-10f);

        TrafficData trafficData = new TrafficData(
                "Via Cambridge", 30f);
        ParkingData parkingData = new ParkingData(
                1111, "Via Cambridge", 30f);

        Float expected_toIncrease = 10f;
        Float expected_toDecrease = 0f;

        List<Float> gap = computeAdaptationGaps(anomalies, trafficData, parkingData);

        assertEquals(gap.get(0), expected_toIncrease);
        assertEquals(gap.get(1), expected_toDecrease);
    }

    /**
     * Test if with excess anomalies gap to decrease is computed.
     */
    @Test
    public void Test3_gapWithAnomaliesAndParkingAndTraffic() throws Exception {

        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.WEATHER_MORE,15f);
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS,-5f);

        TrafficData trafficData = new TrafficData("Via Cambridge", 0f);
        ParkingData parkingData = new ParkingData(1111, "Via Cambridge", 0f);

        Float expected_toIncrease = 5f;
        Float expected_toDecrease = -15f;

        List<Float> gap = computeAdaptationGaps(anomalies, trafficData, parkingData);

        assertEquals(gap.get(0), expected_toIncrease);
        assertEquals(gap.get(1), expected_toDecrease);
    }

    private List<Float> computeAdaptationGaps(HashMap<MalfunctionType, Float> anomalies,
                                              TrafficData trafficData, ParkingData parkingData) {

        Float anomalyGap;
        Float toIncreaseGap = 0f;
        Float toDecreaseGap = 0f;

        if (!lampDamaged(anomalies)) {

            if ((anomalyGap = anomalies.get(MalfunctionType.WEATHER_LESS)) != null)
                toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.WEATHER_LESS

            if ((anomalyGap = anomalies.get(MalfunctionType.WEATHER_MORE)) != null)
                toDecreaseGap = Math.min(toDecreaseGap, -anomalyGap);    // MalfunctionType.WEATHER_MORE

            if ((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS)) != null)
                toIncreaseGap = Math.max(toIncreaseGap, -anomalyGap);   // MalfunctionType.LIGHT_INTENSITY_LESS

            if ((anomalyGap = anomalies.get(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE)) != null)
                toDecreaseGap = Math.min(toDecreaseGap, -anomalyGap);    // MalfunctionType.LIGHT_INTENSITY_MORE

            if (!changeRequired(toIncreaseGap, toDecreaseGap, trafficData, parkingData)) {
                List<Float> gap = new ArrayList<>(2);
                gap.add(0f);
                gap.add(0f);
                return gap;
            }
        }
        List<Float> gap = new ArrayList<>(2);
        gap.add(0,toIncreaseGap);
        gap.add(1,toDecreaseGap);
        return gap;
    }

    /**
     * Check if the lamp has DAMAGED_BULB or NOT_RESPONDING type of anomaly.
     *
     * @param anomalies map between malfunction type and difference gap from correct value
     * @return true is lamp damaged
     */
    private boolean lampDamaged(HashMap<MalfunctionType, Float> anomalies) {

        return anomalies.get(MalfunctionType.NOT_RESPONDING) != null
                || anomalies.get(MalfunctionType.DAMAGED_BULB) != null;
    }

    /**
     * Check if data observed require an adapting operation.
     */
    private boolean changeRequired(Float toIncreaseGap, Float toDecreaseGap,
                                   TrafficData trafficData, ParkingData parkingData) {
        return !toIncreaseGap.equals(0f)
                || !toDecreaseGap.equals(0f)
                || trafficData.getCongestionPercentage() > traffic_tolerance
                || parkingData.getOccupationPercentage() > parking_tolerance;
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended AnalyzeBolt Test");
    }

}