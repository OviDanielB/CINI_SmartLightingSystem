package org.uniroma2.sdcc.Example;

import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.unit.DegreeUnit;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ovidiudanielbarba on 23/03/2017.
 */
public class App {
    public static void main(String[] args){

        /*
        Map<String, Float> streetAverageConsumption = new HashMap<>();
        streetAverageConsumption.put("via",2.3f);
        //streetAverageConsumption.putIfAbsent("via",2.2f);

        streetAverageConsumption.replace("via",3.0f);


        System.out.println(streetAverageConsumption.toString());
        */

        try {
            YahooWeatherService yahoo = new YahooWeatherService();
            Channel channel = yahoo.getForecast("721943", DegreeUnit.CELSIUS);

            System.out.println(channel.getAtmosphere().toString());
            System.out.println(channel.getItem().toString());
            System.out.println(channel.getAstronomy());


        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
