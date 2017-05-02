package org.uniroma2.sdcc.Utils;


import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

/**
 * Helper class to manage Tick Tuple Storm feature.
 */
public class TupleHelpers {


    /** Check if the received tuple is a Tick Tuple
     *
     * @param tuple tuple to check
     **/
    public static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }

    /**
     * Configuration of Tick Tuple frequency.
     *
     * @param tickFrequencyInSeconds TickTuple frequency
     * @return configuration
     */
    public static Map<String, Object> getTickTupleFrequencyConfig(int tickFrequencyInSeconds) {
        Config conf = new Config();
        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickFrequencyInSeconds);
        return conf;
    }
}
