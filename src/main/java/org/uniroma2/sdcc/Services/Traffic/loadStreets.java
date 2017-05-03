package org.uniroma2.sdcc.Services.Traffic;

import org.uniroma2.sdcc.Model.TrafficData;

import java.io.*;
import java.util.List;

/**
 * Load all streets from file formatted as a street for line
 * and save them in DynamoDB to be available for TrafficREST API.
 */
public class loadStreets {

    // TODO remove
    public static void main(String[] args) {

        StreetTrafficREST streetTrafficREST = new StreetTrafficREST();
        List<TrafficData> old_streets = streetTrafficREST.getAllCityStreetsTraffic();

        // delete old streets
        for (TrafficData t: old_streets) {
            streetTrafficREST.deleteStreet(t.getStreet());
        }

        File f = new File("src/main/java/org/uniroma2/sdcc/Traffic/streets.txt");

        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String readLine = "";

        System.out.println("Reading file using Buffered Reader");

        try {
            while ((readLine = b.readLine()) != null) {
                streetTrafficREST.insertNewStreet(readLine, Math.round(Math.random()*1000)/10f);
                System.out.println(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
