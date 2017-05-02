package org.uniroma2.sdcc.ControlSystem;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.uniroma2.sdcc.Utils.Cache.CacheManager;
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.Config.ServiceConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;
import java.util.TimerTask;


/**
 * This component request every 10 seconds through Parking REST API
 * values of percentage of current parking occupation of all car parks' cells.
 */
public class ParkingSource extends TimerTask {

    private static String PARKING_SERVER_DEFAULT = "localhost";
    private static int PARKING_PORT_DEFAULT = 3000;

    org.apache.http.client.HttpClient httpClient;

    private CacheManager cache;

    private String parking_host = PARKING_SERVER_DEFAULT;
    private int parking_port = PARKING_PORT_DEFAULT;

    private String REST_URL;

    public ParkingSource() {
        config();
        cache = new MemcachedManager();
        httpClient = HttpClientBuilder.create().build();
    }

    /**
     * Configuration.
     */
    private void config() {

        YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();

        try {

            ServiceConfig parkingConfig = yamlConfigRunner.getConfiguration().
                    getParkingServer();


            parking_host = parkingConfig.getHostname();
            parking_port = parkingConfig.getPort();

        } catch (IOException e) {
            e.printStackTrace();
        }

        REST_URL = "http://" + parking_host + ":" + parking_port + "/";
    }

    @Override
    public void run() {
        String responseBody = getParkingOccupation();
        saveCurrentData(responseBody);
    }

    /**
     * Save current parking occupation information in memory.
     *
     * @param responseBody body of response to GET request
     */
    private void saveCurrentData(String responseBody) {

        if (responseBody != null) {
            cache.put(MemcachedManager.PARKING_LIST_KEY,responseBody);
        }
    }

    /**
     * Request for list of parking occupation percentages by cell ids
     *
     * @return response body to GET request
     */
    private String getParkingOccupation() {
        HttpGet httpGet = new HttpGet(REST_URL);

        // Execute HTTP GET Request to ParkingServer
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}


