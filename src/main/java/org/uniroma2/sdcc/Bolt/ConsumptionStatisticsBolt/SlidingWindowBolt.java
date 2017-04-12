package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.log4j.BasicConfigurator;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseRichBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uniroma2.sdcc.Utils.NthLastModifiedTimeTracker;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;

import java.util.Map;

/**
 * Storm bolt receives street-lamp data and computes statistics on energy consumption.
 * The statistics are estimate into a window of time.
 *
 * @author Emanuele , Ovidiu , Laura , Marius
 * @see SlidingWindowAvg
 */
public abstract class SlidingWindowBolt<T> extends BaseRichBolt {

    /* Default values */
    protected static final int NUM_WINDOW_CHUNKS = 60;
    protected static final int DEFAULT_SLIDING_WINDOW_IN_SECONDS = 3600;
    protected static final int DEFAULT_SLIDING_DURATION_IN_SECONDS = DEFAULT_SLIDING_WINDOW_IN_SECONDS / NUM_WINDOW_CHUNKS;
    protected static final String WINDOW_LENGTH_WARNING_TEMPLATE = "Actual window length is %d seconds when it should" +
            " be %d seconds (you can safely ignore this warning during the startup phase)";

    protected SlidingWindowAvg<T> window;               // keep computation for street-lamp statistics
    protected int windowLengthInSeconds;                // the length or duration of the window
    protected int emitFrequencyInSeconds;               // the interval at which the windowing slides
    protected int tickFrequencyInSeconds;               // the interval between two tick tuple


    protected NthLastModifiedTimeTracker lastModifiedTracker; // keep track for last window slide
    protected OutputCollector collector;
    protected Logger logger;


    /**
     * @param windowLengthInSeconds  Windows length into which the statistics are computed
     * @param tickFrequencyInSeconds How often the statistics are emitted
     */
    public SlidingWindowBolt(int windowLengthInSeconds, int tickFrequencyInSeconds, int emitFrequencyInSeconds) {

        if (windowLengthInSeconds <= 0 || tickFrequencyInSeconds <= 0)
            throw new IllegalArgumentException("windows length and emit frequency must be > 0");

        this.windowLengthInSeconds = windowLengthInSeconds;
        this.tickFrequencyInSeconds = tickFrequencyInSeconds;
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;

        window = new SlidingWindowAvg<>(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
                this.tickFrequencyInSeconds), tickFrequencyInSeconds);

    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        logger = LoggerFactory.getLogger(SlidingWindowBolt.class);
        lastModifiedTracker = new NthLastModifiedTimeTracker(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
                this.tickFrequencyInSeconds));
    }

    protected int deriveNumWindowChunksFrom(int windowLengthInSeconds, int windowUpdateFrequencyInSeconds) {
        return windowLengthInSeconds / windowUpdateFrequencyInSeconds;
    }

}
