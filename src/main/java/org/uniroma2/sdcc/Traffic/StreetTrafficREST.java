package org.uniroma2.sdcc.Traffic;

import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.model.*;

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

    private static final String CITY = "Rome";
    private static final String TABLE_NAME = "Traffic";
    private final Traffic client;

    private static final String UPDATE_EXPRESSION = "set TrafficPerc = r";

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
        GetStreeTrafficResult result = client.getStreeTraffic(request);

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
    public void insertNewStreet(String street, Float trafficCongPerc) {

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
        client.postStreeTraffic(request);

    }

    /**
     * update traffic congestion percentage on street
     * @param street
     * @param newTraffic new traffic congestion percentage value
     */
    @Override
    public void updateStreetTraffic(String street, Float newTraffic) {

        /* construct PUT request for updating street traffic */
        PutStreeTrafficRequest request = new PutStreeTrafficRequest();

        /* HTTP body json template for DynamoDB update */
        PUTReq jsonModel =  new PUTReq();
        jsonModel.tableName(TABLE_NAME)
                .key(new Key().city(CITY).street(street))
                .updateExpression(UPDATE_EXPRESSION)
                .expressionAttributeValues(new ExpressionAttributeValues().r(newTraffic.doubleValue()));

        /* send PUT request */
        client.putStreeTraffic(request);
    }

    /**
     * delete street
     * @param street
     */
    @Override
    public void deleteStreet(String street) {

        /* create DELETE request for street deletion */
        DeleteStreeTrafficRequest request = new DeleteStreeTrafficRequest();

        /* HTTP body json template for DynamoDB deletion */
        DELETEReq jsonTemplate = new DELETEReq();
        jsonTemplate.tableName(TABLE_NAME)
                .key(new Key().city(CITY).street(street));

        /* assing HTTP message body */
        request.setDELETEReq(jsonTemplate);

        /* send DELETE request */
        client.deleteStreeTraffic(request);

    }

    /**
     * shutdown AWS API gateway connection
     */
    public void closeConn(){
        client.shutdown();
    }
}
