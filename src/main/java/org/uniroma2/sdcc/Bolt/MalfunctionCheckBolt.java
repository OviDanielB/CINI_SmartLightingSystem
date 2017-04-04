package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.MalfunctionType;
import org.uniroma2.sdcc.Model.StreetLampMessage;

import java.sql.Timestamp;
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

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.outputCollector = collector;

        streetAverageConsumption = new HashMap<>();
        probablyMalfunctioningCount = new HashMap<>();
        printTimerStart();
        periodicGlobalAvg();
    }

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

        //StreetLampMessage message = (StreetLampMessage) input.getValueByField(StreetLampMessage.STREET_LAMP_MSG);
        Integer id = (Integer) input.getValueByField(StreetLampMessage.ID);
        Address address = (Address) input.getValueByField(StreetLampMessage.ADDRESS);
        Boolean on = (Boolean) input.getValueByField(StreetLampMessage.ON);
        String model = (String) input.getValueByField(StreetLampMessage.LAMP_MODEL);
        Float consumption = (Float) input.getValueByField(StreetLampMessage.CONSUMPTION);
        Float intensity = (Float) input.getValueByField(StreetLampMessage.INTENSITY);
        Date lifetime = (Date) input.getValueByField(StreetLampMessage.LIFETIME);
        Timestamp timestamp = (Timestamp) input.getValueByField(StreetLampMessage.TIMESTAMP);

        String reducedAddress = composeAddress(address);

        incrementReceivedMessages();

        //countMalfunctioning(on);

        Values values = new Values();
        String malfunctionTypes = "";

        if(lightIntensityAnomalyDetected(reducedAddress,intensity)){
            System.out.println("[CINI] PROBABLE LIGHT INTENSITY ANOMALY DETECTED ON " + address);
            increaseProbablyMalfunctions(id);

            if(almostSurelyMalfunction(id)) {

                malfunctionTypes += MalfunctionType.LIGHT_INTENSITY_ANOMALY.toString() + ";";
            }
        }
        if(damagedBulb(reducedAddress,on)){

            malfunctionTypes += MalfunctionType.DAMAGED_BULB.toString() + ";";
        }

        if(malfunctionTypes.isEmpty()){
            malfunctionTypes += MalfunctionType.NONE.toString() + ";";
        }

        values.add(malfunctionTypes);
        values.add(id);
        values.add(address);
        values.add(on);
        values.add(model);
        values.add(intensity);
        values.add(timestamp);

        outputCollector.emit(input,values);
        outputCollector.ack(input);

        updateStreetLightIntensityAvg(reducedAddress,intensity);

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

        String finalAddress = String.format("%s %s",address.getName());
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
            /* test if it should be on or off (seeing the other lamps on the same street )*/
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
     * @return true if anomaly is detected
     */
    private boolean lightIntensityAnomalyDetected(String address, Float intensity) {

        return streetAverageConsumption.entrySet().stream()
                .filter(e -> e.getKey().equals(address))
                .filter(e -> {

                    Float mean = e.getValue().getCurrentMean();
                    Float stdev = e.getValue().stdDev();

                    Float lowerBound = mean - stdev;
                    Float upperBound = mean + stdev;

                    return (!(intensity >= lowerBound && intensity <= upperBound));
                }).count() > 0 ;
    }

    private void updateStreetLightIntensityAvg(String address, Float intensity) {

        streetAverageConsumption.putIfAbsent(address,new StreetStatistics(0,intensity,0f,0f));

        streetAverageConsumption.entrySet().stream()
                .filter(e -> e.getKey().equals(address))
                .forEach(e -> ricalculateIntensityStatistics(e,intensity));

        System.out.println(streetAverageConsumption.toString());
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
                StreetLampMessage.ADDRESS,StreetLampMessage.ON,StreetLampMessage.LAMP_MODEL,
                StreetLampMessage.INTENSITY, StreetLampMessage.TIMESTAMP));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
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
