package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.TrafficData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Test PlanBolt operation.
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

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(0f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\": \""+trafficData.getStreet()+"\", " +
                "\"congestionPercentage\": \""+ trafficData.getCongestionPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(0f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\": \""+parkingData.getCellID()+"\", " +
                "\"occupationPercentage\": \""+ parkingData.getOccupationPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(20f);
        when(tuple.getFloatByField(Constants.GAP_TO_INCREASE)).thenReturn(10f);
        when(tuple.getFloatByField(Constants.GAP_TO_DECREASE)).thenReturn(-2f);
        when(tuple.getValueByField(Constants.TRAFFIC_BY_ADDRESS)).thenReturn(traffic);
        when(tuple.getValueByField(Constants.PARKING_BY_CELLID)).thenReturn(parking);

        bolt.execute(tuple);
    }

    /**
     * Test the adapt operation if necessary only to increase even if a value
     * to decrease has been measured.
     * @throws Exception
     */
    @Test
    public void adaptByToIncrease() throws Exception {

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(0f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\": \""+trafficData.getStreet()+"\", " +
                "\"congestionPercentage\": \""+ trafficData.getCongestionPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(0f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\": \""+parkingData.getCellID()+"\", " +
                "\"occupationPercentage\": \""+ parkingData.getOccupationPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(20f);
        when(tuple.getFloatByField(Constants.GAP_TO_INCREASE)).thenReturn(10f);
        when(tuple.getFloatByField(Constants.GAP_TO_DECREASE)).thenReturn(-2f);
        when(tuple.getValueByField(Constants.TRAFFIC_BY_ADDRESS)).thenReturn(traffic);
        when(tuple.getValueByField(Constants.PARKING_BY_CELLID)).thenReturn(parking);

        Float expected_intensity = 30f;

        Float test_intensity = bolt.adaptByTrafficLevelAndAnomalies(tuple);

        assertEquals(expected_intensity, test_intensity);
    }

    /**
     * Test the adapt operation if necessary only to decrease if no anomalies and
     * no relevant traffic and parking percentages.
     * @throws Exception
     */
    @Test
    public void adaptByDecrease() throws Exception {

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(5f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\": \""+trafficData.getStreet()+"\", " +
                "\"congestionPercentage\": \""+ trafficData.getCongestionPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(6f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\": \""+parkingData.getCellID()+"\", " +
                "\"occupationPercentage\": \""+ parkingData.getOccupationPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(80f);
        when(tuple.getFloatByField(Constants.GAP_TO_INCREASE)).thenReturn(0f);
        when(tuple.getFloatByField(Constants.GAP_TO_DECREASE)).thenReturn(-20f);
        when(tuple.getValueByField(Constants.TRAFFIC_BY_ADDRESS)).thenReturn(traffic);
        when(tuple.getValueByField(Constants.PARKING_BY_CELLID)).thenReturn(parking);

        Float expected_intensity = 60f;

        Float test_intensity = bolt.adaptByTrafficLevelAndAnomalies(tuple);

        assertEquals(expected_intensity, test_intensity);
    }

    /**
     * Test the adapt operation if necessary only to decrease if no anomalies and
     * no relevant traffic and parking percentages.
     * @throws Exception
     */
    @Test
    public void adaptByTrafficAndParkingAndAnomalies() throws Exception {

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(80f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\": \""+trafficData.getStreet()+"\", " +
                "\"congestionPercentage\": \""+ trafficData.getCongestionPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(40f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\": \""+parkingData.getCellID()+"\", " +
                "\"occupationPercentage\": \""+ parkingData.getOccupationPercentage()+"\", " +
                "\"timestamp\": \""+System.currentTimeMillis()+"\"}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(20f);
        when(tuple.getFloatByField(Constants.GAP_TO_INCREASE)).thenReturn(5f);
        when(tuple.getFloatByField(Constants.GAP_TO_DECREASE)).thenReturn(-10f);
        when(tuple.getStringByField(Constants.TRAFFIC_BY_ADDRESS)).thenReturn(traffic);
        when(tuple.getStringByField(Constants.PARKING_BY_CELLID)).thenReturn(parking);

        Float expected_intensity =
                ((tuple.getFloatByField(Constants.INTENSITY) +
                        tuple.getFloatByField(Constants.GAP_TO_INCREASE)) +
                        trafficData.getCongestionPercentage()*(100 - (tuple.getFloatByField(Constants.INTENSITY) + tuple.getFloatByField(Constants.GAP_TO_INCREASE)))/100)
                        + parkingData.getOccupationPercentage()*(100 - ((tuple.getFloatByField(Constants.INTENSITY) + tuple.getFloatByField(Constants.GAP_TO_INCREASE)) + trafficData.getCongestionPercentage()*(100 - (tuple.getFloatByField(Constants.INTENSITY) + tuple.getFloatByField(Constants.GAP_TO_INCREASE)))/100))/100;

        Float test_intensity = bolt.adaptByTrafficLevelAndAnomalies(tuple);

        assertEquals(expected_intensity, test_intensity);
    }
}