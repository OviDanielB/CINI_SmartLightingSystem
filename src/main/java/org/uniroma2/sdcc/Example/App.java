package org.uniroma2.sdcc.Example;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ovidiudanielbarba on 23/03/2017.
 */
public class App {
    public static void main(String[] args){

        Map<String, Float> streetAverageConsumption = new HashMap<>();
        streetAverageConsumption.put("via",2.3f);
        //streetAverageConsumption.putIfAbsent("via",2.2f);

        streetAverageConsumption.replace("via",3.0f);


        System.out.println(streetAverageConsumption.toString());
    }
}
