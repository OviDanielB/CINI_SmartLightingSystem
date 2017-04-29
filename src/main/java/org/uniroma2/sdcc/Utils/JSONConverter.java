package org.uniroma2.sdcc.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;
import org.uniroma2.sdcc.Model.ParkingData;
import org.uniroma2.sdcc.Model.StreetLampMessage;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;
import org.uniroma2.sdcc.Utils.Ranking.RankingResults;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper to conversion from/to JSON format.
 */
public class JSONConverter {

    private static Gson gson = new Gson();
    private static Type listParkingDataType = new TypeToken<List<ParkingData>>(){}.getType();
    private static Type listTrafficDataType = new TypeToken<List<TrafficData>>(){}.getType();
    private static Type listRankLampType = new TypeToken<List<RankLamp>>(){}.getType();
    private static Type adaptedLampType = new TypeToken<HashMap<String,Integer>>(){}.getType();

    /**
     * Convert object TrafficData in JSON format.
     *
     * @param trafficData class to convert
     * @return json string
     */
    public static String fromTrafficData(TrafficData trafficData) {

        if (trafficData != null)
            return gson.toJson(trafficData);
        return null;
    }

    /**
     * Convert from JSON format to object TrafficData.
     *
     * @param json string to parse
     * @return TrafficData object
     */
    public static TrafficData toTrafficData(String json) {

        try {
            return gson.fromJson(json, TrafficData.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object List<TrafficData> in JSON format.
     *
     * @param trafficDataList class to convert
     * @return json string
     */
    public static String fromTrafficDataList(List<TrafficData> trafficDataList) {

        if (trafficDataList != null)
            return gson.toJson(trafficDataList);
        return null;
    }

    /**
     * Convert from JSON format to object List<TrafficData>.
     *
     * @param json string to parse
     * @return List<TrafficData> object
     */
    public static List<TrafficData> toTrafficDataListData(String json) {
        if(json == null){
            return null;
        }
        try {
            return gson.fromJson(json, listTrafficDataType);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object ParkingData in JSON format.
     *
     * @param parkingData class to convert
     * @return json string
     */
    public static String fromParkingData(ParkingData parkingData) {

        if (parkingData != null)
            return gson.toJson(parkingData);
        return null;
    }

    /**
     * Convert from JSON format to object ParkingData.
     *
     * @param json string to parse
     * @return ParkingData object
     */
    public static ParkingData toParkingData(String json) {

        try {
            return gson.fromJson(json, ParkingData.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object List<ParkingData> in JSON format.
     *
     * @param parkingDataList class to convert
     * @return json string
     */
    public static String fromParkingDataList(List<ParkingData> parkingDataList) {

        if (parkingDataList != null)
            return gson.toJson(parkingDataList);
        return null;
    }

    /**
     * Convert from JSON format to object List<ParkingData>.
     *
     * @param json string to parse
     * @return List<ParkingData> object
     */
    public static List<ParkingData> toParkingDataListData(String json) {
        if(json == null){
            return null;
        }
        try {
            return gson.fromJson(json, listParkingDataType);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object List<RankLamp> in JSON format.
     *
     * @param rankLampList class to convert
     * @return json string
     */
    public static String fromRankLampList(List<RankLamp> rankLampList) {

        if (rankLampList != null)
            return gson.toJson(rankLampList);
        return null;
    }

    /**
     * Convert from JSON format to object List<RankLamp>.
     *
     * @param json string to parse
     * @return List<RankLamp> object
     */
    public static List<RankLamp> toRankLampListData(String json) {

        if (json == null) {
            return new ArrayList<>();
        }
        try {
            return gson.fromJson(json, listRankLampType);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Convert object StreetLampMessage in JSON format.
     *
     * @param streetLampMessage class to convert
     * @return json string
     */
    public static String fromStreetLampMessage(StreetLampMessage streetLampMessage) {

        if (streetLampMessage != null)
            return gson.toJson(streetLampMessage);
        return null;
    }

    /**
     * Convert from JSON format to object TrafficData.
     *
     * @param json string to parse
     * @return StreetLampMessage object
     */
    public static StreetLampMessage toStreetLampMessage(String json) {

        try {
            return gson.fromJson(json, StreetLampMessage.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object RankingResults in JSON format.
     *
     * @param rankingResults class to convert
     * @return json string
     */
    public static String fromRankingResults(RankingResults rankingResults) {

        if (rankingResults != null)
            return gson.toJson(rankingResults);
        return null;
    }

    /**
     * Convert from JSON format to object TrafficData.
     *
     * @param json string to parse
     * @return RankingResults object
     */
    public static RankingResults toRankingResults(String json) {

        try {
            return gson.fromJson(json, RankingResults.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object AnomalyStreetLampMessage in JSON format.
     *
     * @param anomalyStreetLampMessage class to convert
     * @return json string
     */
    public static String fromAnomalyStreetLampMessage(AnomalyStreetLampMessage anomalyStreetLampMessage) {

        if (anomalyStreetLampMessage != null)
            return gson.toJson(anomalyStreetLampMessage);
        return null;
    }

    /**
     * Convert JSON format to object AnomalyStreetLampMessage.
     *
     * @param json string
     * @return AnomalyStreetLampMessage
     */
    public static AnomalyStreetLampMessage toAnomalyStreetLampMessage(String json) {

        try {
            return gson.fromJson(json, AnomalyStreetLampMessage.class);
        } catch (JsonParseException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert object HashMap<String, Integer> in JSON format.
     *
     * @param adapted_lamp class to convert
     * @return json string
     */
    public static String fromAdaptedLamp(HashMap<String, Integer> adapted_lamp) {

        if (adapted_lamp != null)
            return gson.toJson(adapted_lamp);
        return null;
    }

    /**
     * Convert JSON format to object HashMap<String, Integer>.
     *
     * @param json string
     * @return adapted_lamp class to convert
     */
    public static HashMap<String,Integer> toAdaptedLamp(String json) {

        if (json != null)
            return gson.fromJson(json, adaptedLampType);
        return null;
    }

    /**
     * Convert object Tuple in JSON format.
     *
     * @param tuple class to convert
     * @return json string
     */
    public static String fromTuple(Tuple tuple) {

        if (tuple != null) {
            return gson.toJson(tuple);
        }
        return null;
    }
}
