package org.uniroma2.sdcc.Utils;

import org.junit.Test;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test JSONConverter operations.
 */
public class JSONConverterTest {

    /**
     * Check if objects are correctly converted as JSON format and
     * then if these JSON format results are correctly converted back
     * to beginning objects.
     *
     * @throws Exception
     */
    @Test
    public void fromAndToTypeCorrect() throws Exception {

        StreetLamp streetLamp = new StreetLamp(1111,true,Lamp.LED, new Address("VIA CAMBRIDGE",17, AddressNumberType.CIVIC),2222,50f,70f,LocalDateTime.now().minusDays(20));
        TrafficData trafficData = new TrafficData("VIA CAMBRIDGE", 60f);
        ParkingData parkingData = new ParkingData(1111, "VIA CAMBRIDGE", 70f);
        List<TrafficData> trafficDataList = new ArrayList<>(1);
        trafficDataList.add(trafficData);
        List<ParkingData> parkingDataList = new ArrayList<>(1);
        parkingDataList.add(parkingData);
        List<RankLamp> rankLampList = new ArrayList<>(1);
        rankLampList.add(new RankLamp(streetLamp.getID(), streetLamp.getAddress(), streetLamp.getLifetime(), System.currentTimeMillis()));
        StreetLampMessage streetLampMessage = new StreetLampMessage(streetLamp, 70f, System.currentTimeMillis());
        RankingResults rankingResults = new RankingResults(rankLampList,1);
        HashMap<MalfunctionType,Float> anomalies = new HashMap<>();
        anomalies.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE,-20f);
        AnomalyStreetLampMessage anomalyStreetLampMessage = new AnomalyStreetLampMessage(streetLamp, 70f, System.currentTimeMillis(), anomalies,1l);
        HashMap<String,Integer> adaptedLamp = new HashMap<>(2);
        adaptedLamp.put("id", streetLamp.getID());
        adaptedLamp.put("intensity", 60);

        String json_trafficData = JSONConverter.fromTrafficData(trafficData);
        String json_parkingData = JSONConverter.fromParkingData(parkingData);
        String json_trafficDataList = JSONConverter.fromTrafficDataList(trafficDataList);
        String json_parkingDataList = JSONConverter.fromParkingDataList(parkingDataList);
        String json_streetLampMessage = JSONConverter.fromStreetLampMessage(streetLampMessage);
        String json_anomalyStreetLampMessage = JSONConverter.fromAnomalyStreetLampMessage(anomalyStreetLampMessage);
        String json_adaptedLamp = JSONConverter.fromAdaptedLamp(adaptedLamp);
        String json_rankingResults = JSONConverter.fromRankingResults(rankingResults);
        String json_rankLampList = JSONConverter.fromRankLampList(rankLampList);

        /* test conversion to JSON format */
        assertNotNull(json_trafficData);
        assertNotNull(json_parkingData);
        assertNotNull(json_trafficDataList);
        assertNotNull(json_parkingDataList);
        assertNotNull(json_streetLampMessage);
        assertNotNull(json_anomalyStreetLampMessage);
        assertNotNull(json_adaptedLamp);
        assertNotNull(json_rankingResults);
        assertNotNull(json_rankLampList);

        /* test conversion from JSON format */

        assertTrue(trafficData.equals(JSONConverter.toTrafficData(json_trafficData)));
        assertTrue(parkingData.equals(JSONConverter.toParkingData(json_parkingData)));

        List<TrafficData> results_traffic = JSONConverter.toTrafficDataListData(json_trafficDataList);
        assertTrue(trafficDataList.stream().filter(t -> {
            Integer index = trafficDataList.indexOf(t);
            return t.equals(results_traffic.get(index));
        }).count() == trafficDataList.size());

        List<ParkingData> results_parking = JSONConverter.toParkingDataListData(json_parkingDataList);
        assertTrue(parkingDataList.stream().filter(t -> {
                        Integer index = parkingDataList.indexOf(t);
                        return t.equals(results_parking.get(index));
                    }).count() == parkingDataList.size());

        assertTrue(streetLampMessage.equals(JSONConverter.toStreetLampMessage(json_streetLampMessage)));
        assertTrue(anomalyStreetLampMessage.equals(JSONConverter.toAnomalyStreetLampMessage(json_anomalyStreetLampMessage)));
        assertTrue(adaptedLamp.equals(JSONConverter.toAdaptedLamp(json_adaptedLamp)));
        assertTrue(rankingResults.equals(JSONConverter.toRankingResults(json_rankingResults)));

        List<RankLamp> results_ranking = JSONConverter.toRankLampListData(json_rankLampList);
        assertTrue(rankLampList.stream().filter(t -> {
                        Integer index = rankLampList.indexOf(t);
                        return t.equals(results_ranking.get(index));
                    }).count() == rankLampList.size());
    }

    /**
     * Check if null results when null object are passed.
     * @throws Exception
     */
    @Test
    public void fromTypeError() throws Exception {

        assertNull(JSONConverter.fromTrafficData(null));
        assertNull(JSONConverter.fromParkingData(null));
        assertNull(JSONConverter.fromTrafficDataList(null));
        assertNull(JSONConverter.fromParkingDataList(null));
        assertNull(JSONConverter.fromStreetLampMessage(null));
        assertNull(JSONConverter.fromAnomalyStreetLampMessage(null));
        assertNull(JSONConverter.fromTuple(null));
        assertNull(JSONConverter.fromRankingResults(null));
        assertNull(JSONConverter.fromRankLampList(null));
    }

    /**
     * Check if null results when JSON parse error.
     * @throws Exception
     */
    @Test
    public void toTypeError() throws Exception {

        assertNull(JSONConverter.toTrafficData("1{}"));
        assertNull(JSONConverter.toParkingData("1{}"));
        assertNull(JSONConverter.toTrafficDataListData("1{}"));
        assertNull(JSONConverter.toParkingDataListData("1{}"));
        assertNull(JSONConverter.toStreetLampMessage("1{}"));
        assertNull(JSONConverter.toAnomalyStreetLampMessage("1{}"));
        assertNull(JSONConverter.toRankingResults("1{}"));
        assertTrue(JSONConverter.toRankLampListData("1{}").equals(new ArrayList<>()));
    }
}