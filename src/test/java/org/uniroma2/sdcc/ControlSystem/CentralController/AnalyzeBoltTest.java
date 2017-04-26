package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Test AnalyzeBolt operation.
 */
public class AnalyzeBoltTest {

    private AnalyzeBolt bolt;
    private Gson gson;

    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector outputCollector;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        gson = new Gson();

        bolt = new AnalyzeBolt();
        bolt.prepare(new Config(),topologyContext,outputCollector);
    }

    /**
     * Check value saved in memory is equal to that read from memory
     * and prepare value to adapt operation.
     */
    @Test
    public void checkMemoryWrite() {

        String trafficList ="[{\"street\":\"VIA CAMBRIDGE\"," +
                "\"congestionPercentage\":40.0,"+
                "\"timestamp\":1492785909974}," +
                "{\"street\":\"VIA POLITECNICO\",\" +\n" +
                "               \"congestionPercentage\":50.0,"+
                "               \"timestamp\":1492785909974}]";

        String parkingList ="[{\"cellID\":1111," +
                "\"occupationPercentage\":50.0,"+
                "\"timestamp\":1492785909974}," +
                "{\"cellID\":\"VIA POLITECNICO\",\" +\n" +
                "               \"occupationPercentage\":50.0,"+
                "               \"timestamp\":1492785909974}]";

        /* methods tested elsewhere  */
        CacheManager cache = new MemcachedManager("localhost",11211);

        cache.put(MemcachedManager.TRAFFIC_LIST_KEY, trafficList);
        cache.put(MemcachedManager.PARKING_LIST_KEY, parkingList);

        assertEquals(trafficList, cache.getString(MemcachedManager.TRAFFIC_LIST_KEY));
        assertEquals(parkingList, cache.getString(MemcachedManager.PARKING_LIST_KEY));
    }

    /**
     * Test if a lamp is considered as damaged when NOT_RESPONDING type
     * of anomaly is noticed.
     */
    @Test
    public void lampDamaged() {
        HashMap<MalfunctionType,Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.NOT_RESPONDING, 1f);

        assertTrue(bolt.lampDamaged(anomalies));
    }

    /**
     * Test if a lamp is not considered as damaged when NOT_RESPONDING
     * or DAMAGED_BULB anomaly types are not noticed.
     */
    @Test
    public void lampNotDamaged() {
        HashMap<MalfunctionType,Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE, 10f);
        anomalies.put(MalfunctionType.WEATHER_LESS, -10f);

        assertFalse(bolt.lampDamaged(anomalies));
    }

    /**
     * Test the main execute method;
     * functionalities tested elsewhere
     * @throws Exception
     */
    @Test
    public void execute() throws Exception {

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getIntegerByField(Constants.CELL)).thenReturn(2222);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(40f);
        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS, -20f);
        anomalies.put(MalfunctionType.DAMAGED_BULB,1f);
        when(tuple.getValueByField(Constants.MALFUNCTIONS_TYPE)).thenReturn(anomalies);

        bolt.execute(tuple);
    }

    /**
     * Verify if correct values to adapt are computed.
     */
    @Test
    public void changeRequired() {

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(0f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\":\""+trafficData.getStreet()+"\"," +
                "\"congestionPercentage\":"+ trafficData.getCongestionPercentage()+"," +
                "\"timestamp\":1492785909974}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(0f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\":"+parkingData.getCellID()+"," +
                "\"occupationPercentage\":"+ parkingData.getOccupationPercentage()+"," +
                "\"timestamp\":1492785909974}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getIntegerByField(Constants.CELL)).thenReturn(2222);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(40f);
        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS, -20f);
        anomalies.put(MalfunctionType.WEATHER_MORE, 5f);
        when(tuple.getValueByField(Constants.MALFUNCTIONS_TYPE)).thenReturn(anomalies);

        Values expected_values = new Values();
        expected_values.add(tuple.getIntegerByField(Constants.ID));
        expected_values.add(tuple.getFloatByField(Constants.INTENSITY));
        expected_values.add(20f);
        expected_values.add(-5f);
        expected_values.add(traffic);
        expected_values.add(parking);

        Values test_values = bolt.changeRequired(tuple);

        for (int i=0; i<expected_values.size()-2;i++) {
            assertEquals(expected_values.get(i), test_values.get(i));
        }

        assertEquals(gson.fromJson(traffic, TrafficData.class).getStreet(),
                gson.fromJson(test_values.get(4).toString(), TrafficData.class).getStreet());
        assertEquals(gson.fromJson(traffic, TrafficData.class).getCongestionPercentage(),
                gson.fromJson(test_values.get(4).toString(), TrafficData.class).getCongestionPercentage());
        assertEquals(gson.fromJson(traffic, TrafficData.class).getStreet(),
                gson.fromJson(test_values.get(4).toString(), TrafficData.class).getStreet());
        assertEquals(gson.fromJson(traffic, TrafficData.class).getCongestionPercentage(),
                gson.fromJson(test_values.get(4).toString(), TrafficData.class).getCongestionPercentage());

        assertEquals(gson.fromJson(parking, ParkingData.class).getCellID(),
                gson.fromJson(test_values.get(5).toString(), ParkingData.class).getCellID());
        assertEquals(gson.fromJson(parking, ParkingData.class).getOccupationPercentage(),
                gson.fromJson(test_values.get(5).toString(), ParkingData.class).getOccupationPercentage());
        assertEquals(gson.fromJson(parking, ParkingData.class).getCellID(),
                gson.fromJson(test_values.get(5).toString(), ParkingData.class).getCellID());
        assertEquals(gson.fromJson(parking, ParkingData.class).getOccupationPercentage(),
                gson.fromJson(test_values.get(5).toString(), ParkingData.class).getOccupationPercentage());
    }

    /**
     * Test if lamp damaged no values to adapt are computed, even if
     * can have some anomalies.
     */
    @Test
    public void changeNotRequired() {

        TrafficData trafficData = mock(TrafficData.class, withSettings().serializable());
        when(trafficData.getStreet()).thenReturn("VIA CAMBRIDGE");
        when(trafficData.getCongestionPercentage()).thenReturn(0f);
        when(trafficData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String traffic ="{\"street\":\""+trafficData.getStreet()+"\"," +
                "\"congestionPercentage\":"+ trafficData.getCongestionPercentage()+"," +
                "\"timestamp\":1492785909974}";

        ParkingData parkingData = mock(ParkingData.class, withSettings().serializable());
        when(parkingData.getCellID()).thenReturn(2222);
        when(parkingData.getOccupationPercentage()).thenReturn(0f);
        when(parkingData.getTimestamp()).thenReturn(System.currentTimeMillis());
        String parking ="{\"cellID\":"+parkingData.getCellID()+"," +
                "\"occupationPercentage\":"+ parkingData.getOccupationPercentage()+"," +
                "\"timestamp\":1492785909974}";

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getIntegerByField(Constants.CELL)).thenReturn(2222);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(40f);
        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.DAMAGED_BULB, 1f);
        anomalies.put(MalfunctionType.WEATHER_MORE, 5f);
        when(tuple.getValueByField(Constants.MALFUNCTIONS_TYPE)).thenReturn(anomalies);

        assertEquals(null, bolt.changeRequired(tuple));
    }


    /**
     * Test if no change is required if no values to adapt are computed and no
     * relevant traffic congestion and parking occupation is measured.
     */
    @Test
    public void changeNotRequiredWithoutAnomaliesOrParkingOrTraffic() {

        Tuple tuple = mock(Tuple.class);
        when(tuple.getIntegerByField(Constants.ID)).thenReturn(1111);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getIntegerByField(Constants.CELL)).thenReturn(2222);
        when(tuple.getFloatByField(Constants.INTENSITY)).thenReturn(40f);
        HashMap<MalfunctionType, Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.NONE, 1f);
        when(tuple.getValueByField(Constants.MALFUNCTIONS_TYPE)).thenReturn(anomalies);

        assertEquals(null, bolt.changeRequired(tuple));
    }
}