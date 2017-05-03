package org.uniroma2.sdcc.Services.Traffic;

import com.amazonaws.opensdk.SdkRequestConfig;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.Traffic;
import org.uniroma2.sdcc.Services.Traffic.APIGatewaySDK.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * RESTful API for Traffic Data
 * the Traffic client connects to AWS API Gateway using AWS SDK,
 * which triggers a Lambda Function contacting a DynamoDB Table.
 * the constructer istantiates a connection and must be closed
 * with closeConn method
 *
 * Use StreetTraffic interface => class implementation may change :)
 * USAGE:
 *      StreetTraffic traffic = new StreetTrafficREST();
 */
public class StreetTrafficREST implements StreetTraffic {

    private final Traffic client;

    private static final String CITY = "Rome";
    private static final String TABLE_NAME = "Traffic";
    private static final String QUERY_PARAM_TABLE = "tableName";
    private static final String QUERY_PARAM_CITY = "city";
    private static final String QUERY_PARAM_STREET = "street";
    private static final String QUERY_PARAM_NEW_TRAFFIC = "newTraffic";

    public StreetTrafficREST() {
        client = Traffic.builder().build();
    }

    /**
     * return all Streets with relative traffic congestion percentage
     * in city CITY
     * @return list of StreetData
     */
    @Override
    public List<TrafficData> getAllCityStreetsTraffic() {
        List<TrafficData> streetsTraffic = new ArrayList<>();

        /* make new REST GET request */
        GetStreeTrafficRequest request = new GetStreeTrafficRequest();
        request.setTableName(TABLE_NAME);

        /* GET Response => traffic for all city streets */
        GetStreeTrafficResult result;
        try { result = client.getStreeTraffic(request); } catch (TrafficException e){
            e.printStackTrace();
            /* return empty list if there's an error */
            streetsTraffic.clear();
            return streetsTraffic;
        }

        result.getGETTrafficResponse().getItems()
                .forEach(e -> {
                    /* transform to local traffic model */
                    TrafficData s = new TrafficData(e.getStreet(), e.getTrafficPerc().floatValue());
                    streetsTraffic.add(s);
                });

        return streetsTraffic;
    }

    /**
     * add new Street with relative percentage
     * if no specified value => default value = 0;
     * @param street
     * @param trafficCongPerc percentage of congestion on relative
     *                        street
     */
    @Override
    public boolean insertNewStreet(String street, Float trafficCongPerc) {

        /* create insert operation via POST request */
        PostStreeTrafficRequest request = new PostStreeTrafficRequest();

        /* HTTP body json template for DynamoDB insertion */
        POSTReq jsonRequestModel = new POSTReq();
        jsonRequestModel.setTableName(TABLE_NAME);

        if(trafficCongPerc != null) {
            jsonRequestModel.item(new Item().city(CITY).street(street).trafficPerc(trafficCongPerc.doubleValue()));
        } else {
            /* set 0 as default value*/
            jsonRequestModel.item(new Item().city(CITY).street(street).trafficPerc(0d));
        }

        /* add to HTTP message body */
        request.setPOSTReq(jsonRequestModel);

        /* send POST request */
        try{ client.postStreeTraffic(request); } catch (TrafficException e){
            e.printStackTrace();
            return false; /* insertion Failed */
        }

        return true;

    }

    /**
     * update traffic congestion percentage on street
     * @param street
     * @param newTraffic new traffic congestion percentage value
     */
    @Override
    public boolean updateStreetTraffic(String street, Float newTraffic) {

        /* construct PUT request for updating street traffic */
        PutStreeTrafficRequest request = new PutStreeTrafficRequest();

        /* add query parameters */
        SdkRequestConfig.Builder builder = SdkRequestConfig.builder();
        builder.customQueryParam(QUERY_PARAM_TABLE,TABLE_NAME)
                .customQueryParam(QUERY_PARAM_CITY, CITY)
                .customQueryParam(QUERY_PARAM_STREET, street)
                .customQueryParam(QUERY_PARAM_NEW_TRAFFIC,newTraffic.toString());

        /* send PUT request */
        try{ client.putStreeTraffic(request.sdkRequestConfig(builder.build())); } catch (TrafficException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete street
     * @param street
     */
    @Override
    public boolean deleteStreet(String street) {

        /* create DELETE request for street deletion */
        DeleteStreeTrafficRequest request = new DeleteStreeTrafficRequest();


        /* send DELETE request */

       SdkRequestConfig.Builder builder = SdkRequestConfig.builder();
       builder.customQueryParam(QUERY_PARAM_TABLE,TABLE_NAME)
               .customQueryParam(QUERY_PARAM_CITY, CITY)
               .customQueryParam(QUERY_PARAM_STREET, street);

        try{ client.deleteStreeTraffic(request.sdkRequestConfig(builder.build())); } catch (TrafficException e){
            e.printStackTrace();
            return false;
        }

        return true;

    }

    /**
     * shutdown AWS API gateway connection
     */
    @Override
    public void closeConn(){
        client.shutdown();
    }
}
