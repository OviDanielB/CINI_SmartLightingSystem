package org.uniroma2.sdcc.Bolt;

import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Astronomy;
import com.github.fedy2.weather.data.Atmosphere;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.Condition;
import com.github.fedy2.weather.data.unit.DegreeUnit;
import com.github.fedy2.weather.data.unit.Time;
import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.MalfunctionType;
import org.uniroma2.sdcc.Model.StreetLampMessage;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ovidiudanielbarba on 21/03/2017.
 */
public class MalfunctionCheckBolt implements IRichBolt {

    private OutputCollector outputCollector;

    private volatile Map<String, StreetStatistics> streetAverageConsumption;
    private Map<Integer, Integer> probablyMalfunctioningCount;
    private float receivedMessages = 0f;
    private float malfunctioningLamps = 0f;

    private static final Integer NUM_PROBABLE_MALF_THRESHOLD = 5;
    private static final Float ON_PERCENTAGE_THRESHOLD = 0.3f;
    /* WOEID needed for YahooWeather ; 721943 = Rome */
    private static final String CITY_WOEID = "721943";
    private static final Long WEATHER_UPDATE_IN_HOURS = 1L;

    private YahooWeatherService weatherService;
    /* read and write conflict possible */
    private volatile Channel weatherChannel;
    private boolean weatherAvailable = false;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.outputCollector = collector;

        initialization();
        periodicWeatherUpdate();
        printTimerStart();
        periodicGlobalAvg();

    }

    /**
     * update weather forecast periodically;
     * => needed to see if lamp is malfunctioning
     */
    private void periodicWeatherUpdate() {

        /* update weather period */
        long period = 1000 * 60 * 60 * WEATHER_UPDATE_IN_HOURS ;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                    try {
                        weatherService = new YahooWeatherService();
                        weatherChannel = weatherService.getForecast(CITY_WOEID, DegreeUnit.CELSIUS);
                        weatherAvailable = true;
                        System.out.println("[CINI] Weather Service available : " + weatherChannel.toString());

                    } catch (JAXBException | IOException e) {
                        e.printStackTrace();
                        weatherAvailable = false;
                    }

            }
        }, 0, period);

    }

    /**
     * Initialize used variables
     */
    private void initialization() {
        streetAverageConsumption = new HashMap<>();
        probablyMalfunctioningCount = new HashMap<>();
    }

    /**
     * Prints periodic global average of light intensity
     */
    private void periodicGlobalAvg() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                int streetsNum = streetAverageConsumption.size();

                Double globalAvg = streetAverageConsumption.entrySet().stream()
                        .mapToDouble(MalfunctionCheckBolt::getConsValue)
                        .reduce(0, (c,d) -> c + d)
                        / streetsNum; /* divide by number of streets to get average value */


                System.out.println(streetAverageConsumption.toString());
                System.out.println("[CINI] GLOBAL CONSUMPTION AVERAGE = " + globalAvg + "\n");


            }
        }, 30000,10000);
    }

    private static double getConsValue(Map.Entry<String, StreetStatistics> stringAverageStatisticsEntry) {
        return stringAverageStatisticsEntry.getValue().getCurrentMean();
    }


    @Override
    public void execute(Tuple input) {

        Integer id = (Integer) input.getValueByField(StreetLampMessage.ID);
        Address address = (Address) input.getValueByField(StreetLampMessage.ADDRESS);
        Integer cellID = (Integer) input.getValueByField(StreetLampMessage.CELL);
        Boolean on = (Boolean) input.getValueByField(StreetLampMessage.ON);
        String model = (String) input.getValueByField(StreetLampMessage.LAMP_MODEL);
        Float consumption = (Float) input.getValueByField(StreetLampMessage.CONSUMPTION);
        Float intensity = (Float) input.getValueByField(StreetLampMessage.INTENSITY);
        Float naturalLightLevel = (Float) input.getValueByField(StreetLampMessage.NATURAL_LIGHT_LEVEL);
        LocalDateTime lifetime = (LocalDateTime) input.getValueByField(StreetLampMessage.LIFETIME);
        Long timestamp = (Long) input.getValueByField(StreetLampMessage.TIMESTAMP);

        String reducedAddress = composeAddress(address);

        incrementReceivedMessages();

        //countMalfunctioning(on);

        Values values = new Values();

        /* insert new street if not present and update statistics */
        updateStreetLightIntensityAvg(reducedAddress,intensity);

        HashMap<MalfunctionType,Float> malfunctions = new HashMap<>();

        /* check if light intensity is different from the average value of the street (continuously updated) */
        malfunctions = lightIntensityAnomalyDetected(malfunctions, id, reducedAddress,intensity);


        /* checks if the state of the bulb (on or off) is as the most of the
        * lamps on the street ; otherwise it could be damaged */
        if (damagedBulb(reducedAddress,on)) {
            malfunctions.put(MalfunctionType.DAMAGED_BULB, 1f);
        }

        /* checks for weather anomalies (low light on cloudy day, etc) */
        malfunctions = weatherAnomaly(malfunctions, intensity);

        /* check if no anomalies detected */
        if (malfunctions.size() == 0){
            malfunctions.put(MalfunctionType.NONE,1f);
        }


        Gson gson = new Gson();
        // TODO
        values.add(malfunctions);
        values.add(id);
        values.add(address);
        values.add(cellID);
        values.add(on);
        values.add(model);
        values.add(consumption);
        values.add(lifetime);
        values.add(intensity);
        values.add(naturalLightLevel);
        values.add(timestamp);


        outputCollector.emit(input,values);
        outputCollector.ack(input);
    }

    /**
     * DOCS
     * https://developer.yahoo.com/weather/documentation.html
     * detects multiple types of weather anomalies like
     * on cloudy days, on low visibility (foggy) and by
     * looking at the intensity before sunrise and after sunset
     *
     * @param intensity light level
     * @return 0 if no anomaly (AT LEAST ONCE is present)
     *         -1 if lack anomaly (less than necessary)
     *         1 if excess anomaly (more than necessary)
     */
    private HashMap<MalfunctionType,Float> weatherAnomaly(
            HashMap<MalfunctionType,Float> malfunctions, Float intensity) {

        if (!weatherAvailable) {

            return malfunctions;

        } else {

            Atmosphere atmosphere = weatherChannel.getAtmosphere();
            Condition currentCondition = weatherChannel.getItem().getCondition();
            Astronomy astronomy = weatherChannel.getAstronomy();

            Integer conditionCode = currentCondition.getCode();
            Float visibility = atmosphere.getVisibility();

            HashMap<Float,Float> weatherIntensityRight = WeatherHelper.rightIntensityOnWeatherByCode(conditionCode,intensity);
            HashMap<Float,Float> visibilityIntensityRight = WeatherHelper.rightIntensityByVisibility(intensity,visibility);
            HashMap<Float,Float> astronomyIntensityRight = WeatherHelper.rightIntesityByAstronomy(intensity,astronomy);

            Float min_underGap = Math.min(Math.min(
                    weatherIntensityRight.get(WeatherHelper.CLOUDY_SKY_INTENSITY_MINIMUM),
                    visibilityIntensityRight.get(WeatherHelper.VISIBILITY_INTENSITY_MINIMUM)),
                    astronomyIntensityRight.get(WeatherHelper.DARK_SKY_INTENSITY_MINIMUM));
            if (min_underGap < 0) {
                malfunctions.put(MalfunctionType.WEATHER_LESS, min_underGap);
                return malfunctions;
            }

            Float min_overGap = Math.min(Math.min(
                    weatherIntensityRight.get(WeatherHelper.SUNNY_SKY_INTENSITY_MAXIMUM),
                    visibilityIntensityRight.get(WeatherHelper.VISIBILITY_INTENSITY_MAXIMUM)),
                    astronomyIntensityRight.get(WeatherHelper.GLARE_SKY_INTENSITY_MAXIMUM));
            if (min_overGap > 0) {
                malfunctions.put(MalfunctionType.WEATHER_MORE, min_overGap);
                return malfunctions;
            }

            return malfunctions;
        }
    }


    private boolean almostSurelyMalfunction(Integer id) {

        if(probablyMalfunctioningCount.get(id) > NUM_PROBABLE_MALF_THRESHOLD){
            System.out.println("[ALERT] Street Lamp with ID " + id +
                    " exceeded malfunctioning threshold of " + NUM_PROBABLE_MALF_THRESHOLD + "!");
            return true;
        }

        return false;
    }

    /**
     * retain only valuable information for address
     * @param address
     * @return ex: Via Politecnico  (without number)
     */
    private String composeAddress(Address address) {

        String finalAddress = String.format("%s",address.getName());
        return finalAddress;
    }

    /**
     * comparing with the percentage of active lights on the same street,
     * tests if it should be on or off => see if it's damaged or not
     * @param address simple street name (without number or km)
     * @param on the state of the particular light bulb
     * @return true if bulb is damaged,false otherwise
     */
    private boolean damagedBulb(String address,Boolean on) {

        StreetStatistics statistics = streetAverageConsumption.get(address);

        if(on){ /* lamp is on */
            statistics.updateOnPercentage(1f);
            /* test if it should be on or off (seeing the other lamps on the same street )*/
            return !(statistics.getOnPercentage() > (1 - ON_PERCENTAGE_THRESHOLD));

        } else { /* lamp is off */
            statistics.updateOnPercentage(0f);
            /* test if it should be on or off (seeing the other lamps on the same street)*/
            return !(statistics.getOnPercentage() < ON_PERCENTAGE_THRESHOLD);
        }

    }

    private void increaseProbablyMalfunctions(Integer id) {
        // TODO
        probablyMalfunctioningCount.putIfAbsent(id,0);

        /* increment number of probable malfunctions */
        probablyMalfunctioningCount.put(id, probablyMalfunctioningCount.get(id) + 1);

    }

    /**
     *  detects an anomaly on light intensity;
     *  on every street the average value and standard deviation
     *  is constantly updated ;
     *  when a new light intensity value is received, controls if it's
     *  in the inverval [mean - stdev, mean + stdev], meaning it's in the good interval
     * @param address street name
     * @param intensity new value to evaluate
     * @return 0 if no anomaly detected
     *         -1 if anomaly (less than necessary) is detected
     *         1 if anomaly (more than necessary) is detected
     */
    private HashMap<MalfunctionType,Float> lightIntensityAnomalyDetected(
            HashMap<MalfunctionType,Float> malfunctions, int id, String address, Float intensity) {

        // true if intensity more than upper bound
        StreetStatistics streetStatistics = streetAverageConsumption.get(address);
        Float mean = streetStatistics.getCurrentMean();
        Float stdev = streetStatistics.stdDev();
        Float lowerBound = mean - stdev;
        Float upperBound = mean + stdev;
        Float underGap = intensity - lowerBound;
        Float overGap = intensity - upperBound;

        if (underGap < 0) {
            /* avoid errors from light transition (from sunny to cloudy and viceversa) */
            increaseProbablyMalfunctions(id);

            /* the threshold of errors has been overcome =>
                 almost surely it is an anomaly */
            if (almostSurelyMalfunction(id)) {
                malfunctions.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_LESS, underGap);
            }

        } else if (overGap > 0) {
            /* avoid errors from light transition (from sunny to cloudy and viceversa) */
            increaseProbablyMalfunctions(id);

                /* the threshold of errors has been overcome =>
                 almost surely it is an anomaly */
            if (almostSurelyMalfunction(id)) {
                malfunctions.put(MalfunctionType.LIGHT_INTENSITY_ANOMALY_MORE, overGap);
            }
        }
        // no anomaly detected
        return malfunctions;
    }

    private void updateStreetLightIntensityAvg(String address, Float intensity) {

        streetAverageConsumption.putIfAbsent(address,new StreetStatistics(0,intensity,0f,0f));

        streetAverageConsumption.entrySet().stream()
                .filter(e -> e.getKey().equals(address))
                .forEach(e -> ricalculateIntensityStatistics(e,intensity));

       // System.out.println(streetAverageConsumption.toString());
    }

    /**
     * update statistics (average and stdev) for specific address at each incoming tuple
     * algorithm: n=0,avg=0,v=0
     * loop(){
     *     x = getData()
     *     n++
     *     d = x - avg
     *     v = v + d*d*(n-1)/n
     *     avg = avg + d/n
     *
     *     s = sqrt(v/n) // s standard dev
     * }
     *
     * @param e <Address, Statistics> tuple
     * @param intensity latest light intensity value
     */
    private void ricalculateIntensityStatistics(Map.Entry<String, StreetStatistics> e, Float intensity) {

        Integer sampleNum = e.getValue().getSampleNumb();
        Float oldMean = e.getValue().getCurrentMean();
        Float oldV = e.getValue().getCurrentV();
        Float onPercentage = e.getValue().getOnPercentage();

        /* current value minus old mean (needed for further computations) */
        Float d = intensity - oldMean;
        /* n + 1 samples (needed for average)*/
        sampleNum++;


        /* update v value (used for stdev) algorithm */
        Float updatedV = (oldV + d * d * (sampleNum - 1) / sampleNum );

        /* update average from updated values */
        Float updatedMean = oldMean + d / sampleNum;

        // TODO updated statistics instance, not create a new one
        e.setValue(new StreetStatistics(sampleNum, updatedMean, updatedV, onPercentage));


    }

    /**
     * updated number of received messages
     */
    private void incrementReceivedMessages() {
        /* avoid overflow */
        if( receivedMessages == Long.MAX_VALUE){
            receivedMessages = 0f;
            malfunctioningLamps = 0f;
        }

        receivedMessages++;
    }

    /**
     * counts malfunctioning street lamps
     * @param on true if light bulb is working
     */
    private void countMalfunctioning(Boolean on) {

        if(!on){
            malfunctioningLamps++;
        }
    }

    /**
     * print every 30 seconds percentage of malfunctioning street lamps
     */
    private void printTimerStart() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(receivedMessages != 0) {
                    System.out.println("[CINI] " + String.format("%.2f", malfunctioningLamps / receivedMessages * 100) + "% MALFUNCTIONING STREET LAMPS. \n");
                }
            }
        }, 60000,10000);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        declarer.declare(new Fields(StreetLampMessage.MALFUNCTIONS_TYPE,StreetLampMessage.ID,
                StreetLampMessage.ADDRESS,StreetLampMessage.CELL,StreetLampMessage.ON,
                StreetLampMessage.LAMP_MODEL, StreetLampMessage.CONSUMPTION,StreetLampMessage.LIFETIME,
                StreetLampMessage.INTENSITY, StreetLampMessage.NATURAL_LIGHT_LEVEL,
                StreetLampMessage.TIMESTAMP));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }


    /**
     * defines methods for checking right light intensity
     * comparing to given weather conditions
     */
    private static class WeatherHelper {

        public static final Float CLOUDY_SKY_INTENSITY_MINIMUM = 70.0f;
        public static final Float SUNNY_SKY_INTENSITY_MAXIMUM = 50.0f;
        public static final Float VISIBILITY_INTENSITY_MINIMUM = 80.0f;
        public static final Float VISIBILITY_INTENSITY_MAXIMUM = 50.0f;
        public static final Float DARK_SKY_INTENSITY_MINIMUM = 90.0f;
        public static final Float GLARE_SKY_INTENSITY_MAXIMUM = 50.0f;
        public static final Integer[] darkSkyCodes = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,19,20,21,26,27,28,29,30,37,38,39,40,44,45,47};


        public WeatherHelper() {
        }

        private static boolean darkSkyFromCode(Integer code){
            return Arrays.stream(darkSkyCodes).
                    filter( e -> e.equals(code)).count()  > 0;
        }

        private static HashMap<Float,Float> rightIntensityByVisibility(Float intensity,Float visibility) {
            // TODO Visibility Policy
            HashMap<Float,Float> i = new HashMap<>();

            Float underGap = intensity - VISIBILITY_INTENSITY_MINIMUM;
            Float overGap = intensity - VISIBILITY_INTENSITY_MAXIMUM;
            if (!(visibility > .5) && underGap < 0) {
                i.put(VISIBILITY_INTENSITY_MINIMUM, underGap);
                i.put(VISIBILITY_INTENSITY_MAXIMUM, 0f);
                return i;
            } else if (visibility > .5 && overGap > 0) {
                i.put(VISIBILITY_INTENSITY_MINIMUM, 0f);
                i.put(VISIBILITY_INTENSITY_MAXIMUM, overGap);
                return i;
            }
            i.put(VISIBILITY_INTENSITY_MINIMUM, 0f);
            i.put(VISIBILITY_INTENSITY_MAXIMUM, 0f);
            return i;
        }


        public static HashMap<Float, Float> rightIntensityOnWeatherByCode(Integer code,Float intensity){

            HashMap<Float, Float> i = new HashMap<>();
            Float underGap = intensity - CLOUDY_SKY_INTENSITY_MINIMUM;
            Float overGap = intensity - SUNNY_SKY_INTENSITY_MAXIMUM;
            // if cloudy sky
            if (darkSkyFromCode(code)) {
                if ( underGap < 0 ) {
                    i.put(CLOUDY_SKY_INTENSITY_MINIMUM, underGap);
                    i.put(SUNNY_SKY_INTENSITY_MAXIMUM, 0f);
                    return i;
                }
//                    return -1;
                // if sunny sky
            } else if (overGap > 0 ) {
                i.put(SUNNY_SKY_INTENSITY_MAXIMUM, overGap);
                i.put(CLOUDY_SKY_INTENSITY_MINIMUM, 0f);
                return i;
            }
            i.put(CLOUDY_SKY_INTENSITY_MINIMUM, 0f);
            i.put(SUNNY_SKY_INTENSITY_MAXIMUM, 0f);
            return i;
//            } else if (intensity > SUNNY_SKY_INTENSITY_MAXIMUM) {
//                    return 1;
//            }
//            return 0;
        }

        public static HashMap<Float,Float> rightIntesityByAstronomy(Float intensity, Astronomy astronomy) {
            LocalDateTime now = LocalDateTime.now();
            Integer nowHour = now.getHour();
            Integer nowMin = now.getMinute();

            Time sunrise = astronomy.getSunrise();
            Time sunset = astronomy.getSunset();

            HashMap<Float,Float> i = new HashMap<>();
            Float underGap = intensity - DARK_SKY_INTENSITY_MINIMUM;
            Float overGap = intensity - GLARE_SKY_INTENSITY_MAXIMUM;
            /* if it's dark */
            if (nowHour < sunrise.getHours() || nowHour > sunset.getHours()) {
                if (underGap < 0) {
                    i.put(DARK_SKY_INTENSITY_MINIMUM,underGap);
                    i.put(GLARE_SKY_INTENSITY_MAXIMUM, 0f);
                    return i;
                }
            } else if (overGap > 0) {
                i.put(DARK_SKY_INTENSITY_MINIMUM,0f);
                i.put(GLARE_SKY_INTENSITY_MAXIMUM, overGap);
                return i;
            }
            i.put(DARK_SKY_INTENSITY_MINIMUM,0f);
            i.put(GLARE_SKY_INTENSITY_MAXIMUM, 0f);
            return i;
        }
    }


    private static class StreetStatistics {
        private Integer sampleNumb;
        private Float currentMean;
        private Float currentV;
        private Float onPercentage;

        public StreetStatistics(Integer sampleNumb, Float currentValue, Float currentV,Float onPercentage) {
            this.sampleNumb = sampleNumb;
            this.currentMean = currentValue;
            this.currentV = currentV;
            this.onPercentage = onPercentage;
        }

        public StreetStatistics() {
        }

        public Float stdDev(){
            return (float) Math.sqrt(this.currentV / this.sampleNumb );
        }

        public Integer getSampleNumb() {
            return sampleNumb;
        }

        public void setSampleNumb(Integer sampleNumb) {
            this.sampleNumb = sampleNumb;
        }

        public Float getCurrentMean() {
            return currentMean;
        }

        public void setCurrentMean(Float currentMean) {
            this.currentMean = currentMean;
        }

        public Float getCurrentV() {
            return currentV;
        }

        public void setCurrentV(Float currentStdDev) {
            this.currentV = currentStdDev;
        }

        public Float getOnPercentage() {
            return onPercentage;
        }

        public void setOnPercentage(Float onPercentage) {
            this.onPercentage = onPercentage;
        }

        public void updateOnPercentage(Float on){
            this.onPercentage = onPercentage + (on - onPercentage) / (sampleNumb);
        }

        @Override
        public String toString() {
            return "StreetStatistics{" +
                    "sampleNumb=" + sampleNumb +
                    ", currentMean=" + currentMean +
                    ", currentV=" + currentV +
                    ", currentStdDev = " + stdDev() +
                    ", onPercentage = " + onPercentage +
                    '}';
        }
    }
}
