package org.uniroma2.sdcc.ControlSystem;

import com.google.gson.Gson;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.shade.org.apache.http.NameValuePair;
import org.apache.storm.shade.org.apache.http.client.HttpClient;
import org.apache.storm.shade.org.apache.http.client.ResponseHandler;
import org.apache.storm.shade.org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.storm.shade.org.apache.http.client.methods.HttpPost;
import org.apache.storm.shade.org.apache.http.impl.client.BasicResponseHandler;
import org.apache.storm.shade.org.apache.http.impl.client.HttpClientBuilder;
import org.apache.storm.shade.org.apache.http.message.BasicNameValuePair;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.TrafficData;
import org.uniroma2.sdcc.Traffic.StreetTrafficREST;
import org.uniroma2.sdcc.Traffic.TrafficClientBuilder;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * This component request every 10 seconds through a REST interface
 * values of percentage of current traffic level along a street.
 */
public class TrafficSource {

    private static String REST_SERVER = "localhost";
    private static int REST_PORT = 3000;
    private static String REST_URL =
            "http://" + REST_SERVER + ":" + REST_PORT + "/traffic";

    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;

    private static String FILE_PATH = "src/Files/Streets.txt";

    public static void main(String[] args) throws IOException, InterruptedException {

        MemcachedClient memcachedClient =
                new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        Gson gson = new Gson();
        StreetTrafficREST streetTrafficREST = new StreetTrafficREST();

//        try {
            // import streets
//            List<String> json_street_list = readAddresses();

            // asking forever (every 10 seconds) traffic information
            while (true) {

//                for (String street : json_street_list) {
//                    Address address = gson.fromJson(street, Address.class);

//                    HttpClient httpClient = HttpClientBuilder.create().build();
//                    HttpPost httppost = new HttpPost(REST_URL);

                // Add data referring to address
//                    List<NameValuePair> nameValuePairs = new ArrayList<>(1);
//                    nameValuePairs.add(new BasicNameValuePair("address", address.getName()));
//                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
//                    ResponseHandler<String> responseHandler=new BasicResponseHandler();
//                    String responseBody = httpClient.execute(httppost, responseHandler);
//                    String response_json = gson.toJson(responseBody);

                // Save in memory associating a key composed by name of the street
                // written lowercase and with '_' replacing blank spaces
//                    String key = address.getName().toLowerCase().replace(' ','_');
//                    memcachedClient.set("traffic_by_" + key, 36000, response_json);
//                    memcachedClient.set("traffic_list", 36000, responseBody);


                List<TrafficData> street_list = streetTrafficREST.getAllCityStreetsTraffic();
                String json_street_list = gson.toJson(street_list);

                memcachedClient.set("traffic_list", 36000, json_street_list);

                System.out.println("Received: " + json_street_list + "\n");
//                }
                // requesting for data every 10 seconds
                sleep(10000);
            }
//        }
    }

    /**
     * Parse file containing a list of all names of the streets
     * which ask traffic level information for.
     *
     * @return list of read addresses in JSON format
     * @throws IOException
     */
    private static List<String> readAddresses() throws IOException {

        List<String> gson_addr_list = new ArrayList<>();

        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));

        String line;
        line = br.readLine();

        while (line != null) {
            Address address = new Address();
            address.setName(line);
            String gson_address = gson.toJson(address);

            System.out.println(gson_address+"\n");

            gson_addr_list.add(gson_address);
            line = br.readLine();
        }
        br.close();

        return gson_addr_list;
    }
}
