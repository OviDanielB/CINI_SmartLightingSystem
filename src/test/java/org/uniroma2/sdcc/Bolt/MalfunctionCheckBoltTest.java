package org.uniroma2.sdcc.Bolt;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.uniroma2.sdcc.Utils.StreetStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests Malfunction Check Bolt's
 * methods
 */
public class MalfunctionCheckBoltTest {

    String add = "Via Cambdridge";
    HashMap<String, StreetStatistics> streetStats
            = new HashMap<>();
    /* light intensity values */
    ArrayList<Float> values = new ArrayList<>();

    HashMap<Integer, Integer> anomalyCounter = new HashMap<>();
    private StreetStatistics statistics;



    @Before
    public void setUp() throws Exception {
        //streetStats = new HashMap<>();
    }

    /**
     * test for street statistics
     * @throws Exception
     */
    @Test
    public void updateStreetLightIntensityAvg() throws Exception {


        addValues();

        /* for each value update */
        values.stream().forEach(e -> update(add, e));

        Float expected = meanCalc();

        Float calculated = streetStats.get(add).getCurrentMean();


       /* new value don't add a new entry but update it */
        assertTrue(streetStats.size() == 1);
       /* values should be equal */
        assertEquals(expected,calculated , 0.0001f);

       /* on percentage should not change */
        assertTrue(streetStats.get(add).getOnPercentage() == 0);

       /* sample number should be the same as the number of new values provided */
        assertEquals(streetStats.get(add).getSampleNumb() , values.size(), 0);

    }

    private Float meanCalc() {
        Float mean = 0f;
        Optional<Float> opt = values.stream().reduce((e,f) -> e + f );
        if(opt.isPresent()){
            /* expected meanCalc */
            mean = opt.get() / values.size();
        }

        return mean;
    }

    @Test
    public void lightIntensityAnomalyDetected() throws Exception {

        addValues();
        Float mean = meanCalc();
        Float variance =  varianceCalc(mean);
        Float expectedStdDev = (float) Math.sqrt(variance);

        Float inRangeValue = mean + expectedStdDev / 2;
        Float outRangeValue = mean + 2 * expectedStdDev;

        /* for each value update */
        values.stream().forEach(e -> update(add, e));

        Float calculateStd = streetStats.get(add).stdDev();

        /* std dev calculation is correct */
        assertEquals(expectedStdDev,calculateStd,0.01f);
        /* the value is in range */
        assertTrue(inRange(inRangeValue,mean,expectedStdDev));
        /* the value is out of range */
        assertFalse(inRange(outRangeValue,mean,expectedStdDev));

    }

    /**
     * test value if it's in range mean - stdDev
     * @param inRange value to be tested
     * @param mean interval center
     * @param expectedStdDev interval limits
     * @return true if it's in the specified interval, false
     *          otherwise
     */
    private boolean inRange(Float inRange,Float mean, Float expectedStdDev) {

        return ((inRange >= mean - expectedStdDev) && (inRange <= mean + expectedStdDev));
    }

    /**
     * variance calculation
     * @param mean sample mean
     * @return variance
     */
    private Float varianceCalc(Float mean) {

        return values.stream().
                map(e -> (e - mean) * (e - mean)).
                reduce((f,g) -> f + g ).get() / (values.size());
    }

    private void addValues() {
        /* intensity value */
        values.add(90f);
        values.add(80f);
        values.add(60f);
        values.add(85f);
    }



    @Test
    public void updateOnPercentage() throws Exception {
        addPercValues();
        assertEquals(statistics.getOnPercentage(), 0.5, 0);
    }

    @Test
    public void damagedBulb() throws Exception {
        Float ON_PERCENTAGE_THRESHOLD = 0.6f;
        addPercValues();
        Boolean lampLightOnState1 = true;
        Boolean lampLightOnState2 = false; /* it's off*/

        Boolean shouldBeOff;

        /* arrives a new lamp  */
        statistics.setSampleNumb(statistics.getSampleNumb() + 1);
        statistics.updateOnPercentage(1f);

        if(lampLightOnState1){

            shouldBeOff = statistics.getOnPercentage() < ON_PERCENTAGE_THRESHOLD;
            assertFalse(shouldBeOff);
            System.out.println(shouldBeOff);
        }

        if(!lampLightOnState2){
            shouldBeOff = statistics.getOnPercentage() > ON_PERCENTAGE_THRESHOLD;
            System.out.println(shouldBeOff);
        }



    }

    /**
     * add values for ON state percentage
     * calculation
     */
    private void addPercValues() {

        statistics = new StreetStatistics();
        statistics.setOnPercentage(0f); // 0% lights ON

        // 2 samples, 1 ON and 1 OFF
        // onPercentage should be 1/2
        statistics.setSampleNumb(1);
        statistics.updateOnPercentage(1f);
        statistics.setSampleNumb(2);
        statistics.updateOnPercentage(0f);

    }


    private void update(String add, Float intensity){
        StreetStatistics stat =
                new StreetStatistics(0,0f,0f,0f);
        streetStats.putIfAbsent(this.add,stat);

        streetStats.entrySet().stream()
                .filter(e -> e.getKey().equals(add))
                .forEach(e -> {
                    Integer sampleNum = e.getValue().getSampleNumb();
                    Float oldMean = e.getValue().getCurrentMean();
                    Float oldV = e.getValue().getCurrentV();
                    Float onPercentage = e.getValue().getOnPercentage();

                    /* current value minus old meanCalc (needed for further computations) */
                    Float d = intensity - oldMean;
                    /* n + 1 samples (needed for average)*/
                    sampleNum++;


                    /* update v value (used for stdev) algorithm */
                    Float updatedV = (oldV + d * d * (sampleNum - 1) / sampleNum );

                    /* update average from updated values */
                    Float updatedMean = oldMean + d / sampleNum;

                    e.setValue(new StreetStatistics(sampleNum, updatedMean, updatedV, onPercentage));

                });

    }



}