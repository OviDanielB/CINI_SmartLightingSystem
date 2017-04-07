package org.uniroma2.sdcc.Example;


import com.amazonaws.SystemDefaultDnsResolver;
import com.amazonaws.opensdk.SdkRequestConfig;
import com.amazonaws.opensdk.config.ConnectionConfiguration;
import org.uniroma2.sdcc.Traffic.Traffic;
import org.uniroma2.sdcc.Traffic.model.*;

/**
 * Created by ovidiudanielbarba on 23/03/2017.
 */
public class App {
    public static void main(String[] args){

        Traffic client = Traffic.builder().build();

        GetStreeTrafficRequest request = new GetStreeTrafficRequest();
        GetStreeTrafficResult result = client.getStreeTraffic(request.tableName("Traffic"));

        /*
        POSTReq p = new POSTReq().tableName("Traffic").item(new Item().city("Rome").street("Via Paolo I").trafficPerc(new Double(22)));
        PostStreeTrafficRequest request = new PostStreeTrafficRequest();
        request.setPOSTReq(p);
        PostStreeTrafficResult result = client.postStreeTraffic(request);
        */

        result.getGETTrafficResponse().getItems().stream().forEach(e -> System.out.println(e));
        //System.out.println(result.toString());

    }
}
