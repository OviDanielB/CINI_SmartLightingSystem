package org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
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
public abstract class WindowSlidingStatisticsBolt extends BaseRichBolt {

    /* Default values */
    protected static final int NUM_WINDOW_CHUNKS = 60;
    protected static final int DEFAULT_SLIDING_WINDOW_IN_SECONDS = 3600;
    protected static final int DEFAULT_SLIDING_DURATION_IN_SECONDS = DEFAULT_SLIDING_WINDOW_IN_SECONDS / NUM_WINDOW_CHUNKS;
    protected static final String WINDOW_LENGTH_WARNING_TEMPLATE = "Actual window length is %d seconds when it should" +
            " be %d seconds (you can safely ignore this warning during the startup phase)";
    protected static final String STREETLAMP_DISC = "streetLamp";
    protected static final String STREET_DISC = "street";


    protected final SlidingWindowAvg<String> statStreetLamp; // keep computation for street-lamp statistics
    protected final SlidingWindowAvg<String> statStreet;      // keep computation for aggregate street statistics
    protected final int windowLengthInSeconds;                // the length or duration of the window
    protected final int emitFrequencyInSeconds;               // the interval at which the windowing slides


    protected NthLastModifiedTimeTracker lastModifiedTracker; // keep track for last window slide
    protected OutputCollector collector;
    protected Logger logger;


    /**
     * Default constructor
     */
    public WindowSlidingStatisticsBolt() {
        this(DEFAULT_SLIDING_WINDOW_IN_SECONDS, DEFAULT_SLIDING_DURATION_IN_SECONDS);
    }

    /**
     * @param windowLengthInSeconds  Windows length into which the statistics are computed
     * @param emitFrequencyInSeconds How often the statistics are emitted
     */
    public WindowSlidingStatisticsBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {

        if (windowLengthInSeconds <= 0 || emitFrequencyInSeconds <= 0)
            throw new IllegalArgumentException("windows length and emit frequency must be > 0");

        this.windowLengthInSeconds = windowLengthInSeconds;
        this.emitFrequencyInSeconds = emitFrequencyInSeconds;

        statStreetLamp = new SlidingWindowAvg<>(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
                this.emitFrequencyInSeconds), emitFrequencyInSeconds);

        statStreet = new SlidingWindowAvg<>(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
                this.emitFrequencyInSeconds), emitFrequencyInSeconds);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        logger = LoggerFactory.getLogger(WindowSlidingStatisticsBolt.class);
        lastModifiedTracker = new NthLastModifiedTimeTracker(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
                this.emitFrequencyInSeconds));
    }

    @Override
    public void execute(Tuple tuple) {

    }


    /**
     * Slide window and emit computer statistics.
     * Retrieve actual windows-length that in the starting phase can be different from the specified one.
     *
     * @see this#emit(Map, Long, int, String)
     */
    protected void emitCurrentWindowAvgs(SlidingWindowAvg<?> slidingWindow, String discriminator) {
        Map<?, Float> statistics = slidingWindow.getAvgThenAdvanceWindow();

        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
        lastModifiedTracker.markAsModified();

        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
            logger.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
        }

        emit(statistics, this.getWindowEndInSeconds(slidingWindow), actualWindowLengthInSeconds, discriminator);
    }

    /**
     * Emit data in the OutputCollector
     *
     * @param stat                        statistics to emit
     * @param windowEnd                   Instant of time at which statistics computation ends and start new window
     * @param actualWindowLengthInSeconds window Length ( windows end - window start )
     */
    protected void emit(Map<?, Float> stat, Long windowEnd, int actualWindowLengthInSeconds, String discriminator) {
        for (Object key : stat.keySet()) {
            Float avg = stat.get(key);

            collector.emit(new Values(key.toString(), avg, windowEnd, actualWindowLengthInSeconds, discriminator));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("key", "avg", "end window", "window length", "discriminator"));
    }

    protected int deriveNumWindowChunksFrom(int windowLengthInSeconds, int windowUpdateFrequencyInSeconds) {
        return windowLengthInSeconds / windowUpdateFrequencyInSeconds;
    }

    protected Long getWindowEndInSeconds(SlidingWindowAvg wind) {
        return wind.getLastSlideInSeconds() + emitFrequencyInSeconds;
    }

}
