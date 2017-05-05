package org.uniroma2.sdcc.Bolt;

import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Astronomy;
import com.github.fedy2.weather.data.Atmosphere;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.Condition;
import com.github.fedy2.weather.data.unit.DegreeUnit;
import com.github.fedy2.weather.data.unit.Time;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.MalfunctionType;
import org.uniroma2.sdcc.Utils.StreetStatistics;
import org.uniroma2.sdcc.Utils.WeatherHelper;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Bolt that checks for lamp malfunctions and adds them to the message to be
 * sent forward to next bolt
 */
public class MalfunctionCheckBolt implements IRichBolt {

    private OutputCollector outputCollector;

    private volatile Map<String, StreetStatistics> streetStatistics;
    private Map<Integer, Integer> probablyMalfunctioningCount;
    private float receivedMessages = 0f;
    private float malfunctioningLamps = 0f;

    private static final Integer NUM_PROBABLE_MALF_THRESHOLD = 5;
    private volatile Float ON_PERCENTAGE_THRESHOLD = 0.7f;
    /* WOEID needed for YahooWeather ; 721943 = Rome */
    private static final String CITY_WOEID = "721943";
    private static final Long WEATHER_UPDATE_IN_HOURS = 1L;

    private YahooWeatherService weatherService;
    /* read and write conflict possible */
    private volatile Channel weatherChannel;
    private boolean weatherAvailable = false;

    /**
     * Bolt initialization
     *
     * @param stormConf conf
     * @param context   context
     * @param collector collector
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.outputCollector = collector;

        initialization();
        periodicWeatherUpdate();
    }

    /**
     * Bolt operation on incoming tuple.
     *
     * @param input tuple received
     */
    @Override
    public void execute(Tuple input) {

        Integer id = (Integer) input.getValueByField(Constants.ID);
        Address address = (Address) input.getValueByField(Constants.ADDRESS);
        Integer cellID = (Integer) input.getValueByField(Constants.CELL);
        Boolean on = (Boolean) input.getValueByField(Constants.ON);
        String model = (String) input.getValueByField(Constants.LAMP_MODEL);
        Float consumption = (Float) input.getValueByField(Constants.CONSUMPTION);
        Float intensity = (Float) input.getValueByField(Constants.INTENSITY);
        Float naturalLightLevel = (Float) input.getValueByField(Constants.NATURAL_LIGHT_LEVEL);
        LocalDateTime lifetime = (LocalDateTime) input.getValueByField(Constants.LIFETIME);
        Long timestamp = (Long) input.getValueByField(Constants.TIMESTAMP);

        String reducedAddress = composeAddress(address);

        incrementReceivedMessages();

        Values values = new Values();

        /* insert new street if not present and update statistics */
        updateStreetLightIntensityAvg(reducedAddress, intensity);

        HashMap<MalfunctionType, Float> malfunctions = new HashMap<>();

        /* check if light intensity is different from the average value of the street (continuously updated) */
        malfunctions = lightIntensityAnomalyDetected(malfunctions, id, reducedAddress, intensity);


        /* checks if the state of the bulb (on or off) is as the most of the
        * lamps on the street ; otherwise it could be damaged */
        if (damagedBulb(reducedAddress, on)) {
            malfunctions.put(MalfunctionType.DAMAGED_BULB, 1f);
        }

        /* checks for weather anomalies (low light on cloudy day, etc) */
        malfunctions = weatherAnomaly(malfunctions, intensity);

        /* check if no anomalies detected */
        if (malfunctions.size() == 0) {
            malfunctions.put(MalfunctionType.NONE, 1f);
        }

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


        outputCollector.emit(input, values);
        outputCollector.ack(input);
    }

    /**
     * update weather forecast periodically;
     * => needed to see if lamp is malfunctioning
     */
    private void periodicWeatherUpdate() {

        /* update weather period */
        long period = 1000 * 60 * 60 * WEATHER_UPDATE_IN_HOURS;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    weatherService = new YahooWeatherService();
                    weatherChannel = weatherService.getForecast(CITY_WOEID, DegreeUnit.CELSIUS);
                    weatherAvailable = true;
                    updateOnThreshold();

                } catch (JAXBException | IOException e) {
                    e.printStackTrace();
                    weatherAvailable = false;
                }

            }
        }, 0, period);

    }

    /**
     * depending on the weather condition,
     * update threshold of percentage of lamps
     * that should be on
     */
    private void updateOnThreshold() {

        if (WeatherHelper.isDay(weatherChannel) && !WeatherHelper.isCloudy(weatherChannel)) {
            ON_PERCENTAGE_THRESHOLD = 0.3f;
        } else {
            ON_PERCENTAGE_THRESHOLD = 0.7f;
        }
    }

    /**
     * Initialize used variables
     */
    protected void initialization() {
        streetStatistics = new HashMap<>();
        probablyMalfunctioningCount = new HashMap<>();
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
     * -1 if lack anomaly (less than necessary)
     * 1 if excess anomaly (more than necessary)
     */
    protected HashMap<MalfunctionType, Float> weatherAnomaly(
            HashMap<MalfunctionType, Float> malfunctions, Float intensity) {

        if (!weatherAvailable) {

            return malfunctions;

        } else {

            Atmosphere atmosphere = weatherChannel.getAtmosphere();
            Condition currentCondition = weatherChannel.getItem().getCondition();
            Astronomy astronomy = weatherChannel.getAstronomy();

            Integer conditionCode = currentCondition.getCode();
            Float visibility = atmosphere.getVisibility();

            HashMap<Float, Float> weatherIntensityRight = WeatherHelper.rightIntensityOnWeatherByCode(conditionCode, intensity);
            HashMap<Float, Float> visibilityIntensityRight = WeatherHelper.rightIntensityByVisibility(intensity, visibility);
            HashMap<Float, Float> astronomyIntensityRight = WeatherHelper.rightIntesityByAstronomy(intensity, astronomy);

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


    /**
     * verify if light intensity anomaly counter
     * has exceeded THRESHOLD ;
     * if so => almost surely malfunctions
     */
    protected boolean almostSurelyMalfunction(Integer id) {

        if (probablyMalfunctioningCount.get(id) > NUM_PROBABLE_MALF_THRESHOLD) {
            return true;
        }
        return false;
    }

    /**
     * retain only valuable information for address
     *
     * @param address
     * @return ex: Via Politecnico  (without number)
     */
    private String composeAddress(Address address) {

        String finalAddress = String.format("%s", address.getName());
        return finalAddress;
    }

    /**
     * comparing with the percentage of active lights on the same street,
     * tests if it should be on or off => see if it's damaged or not
     *
     * @param address simple street name (without number or km)
     * @param on      the state of the particular light bulb
     * @return true if bulb is damaged,false otherwise
     */
    protected boolean damagedBulb(String address, Boolean on) {

        StreetStatistics statistics = streetStatistics.get(address);

        if (on) { /* lamp is on */
            statistics.updateOnPercentage(1f);
            /* test if it should be on or off (seeing the other lamps on the same street) */
            return statistics.getOnPercentage() < ON_PERCENTAGE_THRESHOLD;

        } else { /* lamp is off */
            statistics.updateOnPercentage(0f);
            /* test if it should be on or off (seeing the other lamps on the same street) */
            return statistics.getOnPercentage() > ON_PERCENTAGE_THRESHOLD;
        }

    }

    /**
     * probably the lamp has a malfunction
     * this probabily is later checked
     *
     * @param id street lamp
     */
    protected void increaseProbablyMalfunctions(Integer id) {
        probablyMalfunctioningCount.putIfAbsent(id, 0);

        /* increment number of probable malfunctions */
        probablyMalfunctioningCount.put(id, probablyMalfunctioningCount.get(id) + 1);

    }

    /**
     * detects an anomaly on light intensity;
     * on every street the average value and standard deviation
     * is constantly updated ;
     * when a new light intensity value is received, controls if it's
     * in the inverval [mean - stdev, mean + stdev], meaning it's in the good interval
     *
     * @param address   street name
     * @param intensity new value to evaluate
     * @return 0 if no anomaly detected
     * -1 if anomaly (less than necessary) is detected
     * 1 if anomaly (more than necessary) is detected
     */
    protected HashMap<MalfunctionType, Float> lightIntensityAnomalyDetected(
            HashMap<MalfunctionType, Float> malfunctions, int id, String address, Float intensity) {

        // true if intensity more than upper bound
        StreetStatistics streetStatistics = this.streetStatistics.get(address);
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
        } else {
            /* decrease counter */
            decreaseProbablyMalfunction(id);
        }
        // no anomaly detected
        return malfunctions;
    }

    /**
     * decrease counter if > 0;
     * if == 0 don't do anything
     * value can't be less than 0
     *
     * @param id street lamp
     */
    private void decreaseProbablyMalfunction(int id) {
        probablyMalfunctioningCount.putIfAbsent(id, 0);

        if (probablyMalfunctioningCount.get(id) > 0) {
            /* decrement number of probable malfunctions */
            probablyMalfunctioningCount.put(id, probablyMalfunctioningCount.get(id) - 1);
        }

    }

    /**
     * update street light intensity statistics based on new received value;
     * insert into HashMap if street not already present, else update
     *
     * @param address   street name
     * @param intensity new value
     */
    protected void updateStreetLightIntensityAvg(String address, Float intensity) {

        streetStatistics.putIfAbsent(address, new StreetStatistics(0, 0f, 0f, 0f));

        streetStatistics.entrySet().stream()
                .filter(e -> e.getKey().equals(address))
                .forEach(e -> ricalculateIntensityStatistics(e, intensity));
    }

    /**
     * update statistics (average and stdev) for specific address at each incoming tuple
     * algorithm: n=0,avg=0,v=0
     * loop(){
     * x = getData()
     * n++
     * d = x - avg
     * v = v + d*d*(n-1)/n
     * avg = avg + d/n
     * <p>
     * s = sqrt(v/n) // s standard dev
     * }
     *
     * @param e         <Address, Statistics> tuple
     * @param intensity latest light intensity value
     */
    protected void ricalculateIntensityStatistics(Map.Entry<String, StreetStatistics> e, Float intensity) {

        Integer sampleNum = e.getValue().getSampleNumb();
        Float oldMean = e.getValue().getCurrentMean();
        Float oldV = e.getValue().getCurrentV();
        Float onPercentage = e.getValue().getOnPercentage();

        /* current value minus old mean (needed for further computations) */
        Float d = intensity - oldMean;
        /* n + 1 samples (needed for average)*/
        sampleNum++;


        /* update v value (used for stdev) algorithm */
        Float updatedV = (oldV + d * d * (sampleNum - 1) / sampleNum);

        /* update average from updated values */
        Float updatedMean = oldMean + d / sampleNum;

        e.setValue(new StreetStatistics(sampleNum, updatedMean, updatedV, onPercentage));


    }

    /**
     * updated number of received messages
     */
    private void incrementReceivedMessages() {
        /* avoid overflow */
        if (receivedMessages == Long.MAX_VALUE) {
            receivedMessages = 0f;
            malfunctioningLamps = 0f;
        }

        receivedMessages++;
    }


    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        declarer.declare(new Fields(
                Constants.MALFUNCTIONS_TYPE,
                Constants.ID,
                Constants.ADDRESS,
                Constants.CELL,
                Constants.ON,
                Constants.LAMP_MODEL,
                Constants.CONSUMPTION,
                Constants.LIFETIME,
                Constants.INTENSITY,
                Constants.NATURAL_LIGHT_LEVEL,
                Constants.TIMESTAMP));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }



}
