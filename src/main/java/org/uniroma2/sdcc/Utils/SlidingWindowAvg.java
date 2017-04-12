package org.uniroma2.sdcc.Utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keep statistics on a specified window of time
 *
 * @param <T> key to which is associated a statistics value
 * @author Emanuele , Ovidiu , Laura , Marius
 */
public class SlidingWindowAvg<T> implements Serializable {

    private static final long serialVersionUID = -2645063988768785810L;

    private static final int WINDOWS_LENGTH_DEF = 24;
    private static final int SLOT_DURATION_IN_SECONDS_DEF = 60;

    private final Map<T, AvgCalculator[]> slottedAvgs = new HashMap<>();

    private int headSlot;                          // current slot
    private int tailSlot;                          // next slot to use
    private int windowLengthInSlots;               // window length in slot
    private int slotDurationInSeconds;             // slot duration in time
    private LocalDateTime lastSlide;               // instant of last slot change

    private ChronoUnit truncation = ChronoUnit.MINUTES;

    /**
     * Default constructor
     */
    public SlidingWindowAvg() {
        this(WINDOWS_LENGTH_DEF, SLOT_DURATION_IN_SECONDS_DEF);
    }

    /**
     * Class constructor
     *
     * @param windowLengthInSlots   windows length in seconds
     * @param slotDurationInSeconds slot length in seconds
     */
    public SlidingWindowAvg(int windowLengthInSlots, int slotDurationInSeconds) {
        if (windowLengthInSlots < 2) {
            throw new IllegalArgumentException("Window length in slots must be at least two (you requested "
                    + windowLengthInSlots + ")");
        }
        this.windowLengthInSlots = windowLengthInSlots + 1;     //plus one to keep incoming data
        this.slotDurationInSeconds = slotDurationInSeconds;

        this.headSlot = 0;
        this.tailSlot = slotAfter(headSlot);
        this.lastSlide = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Update the data structure to obtain new statistics.
     *
     * @param obj         key
     * @param consumption quantity to consider in the statistics
     * @param ts          instant at which the consumption data are measured
     * @throws IllegalArgumentException timestamp's value in the future or too old that doesn't enters in the window
     */
    public void updatedConsumptionAvg(T obj, Float consumption, LocalDateTime ts) throws IllegalArgumentException {

        Integer slot, i;

        LocalDateTime now = LocalDateTime.now();
        if (ts.isAfter(now))
            throw new IllegalArgumentException("The specified timestamp has a value in the future");

        LocalDateTime endHeadSlot = lastSlide.plus(slotDurationInSeconds, ChronoUnit.SECONDS);
        while (ts.isAfter(endHeadSlot) || ts.isEqual(endHeadSlot))
            advanceWindow();

        // According to the timestamp, we found the right slot where consider the consumption value
        for (i = 0; i < windowLengthInSlots; i++) {

            LocalDateTime startSlot = lastSlide.minus(slotDurationInSeconds * i, ChronoUnit.SECONDS);
            LocalDateTime endSlot = lastSlide.plus((1 - i) * slotDurationInSeconds, ChronoUnit.SECONDS);

            if ((ts.isEqual(startSlot) || ts.isAfter(startSlot)) && ts.isBefore(endSlot)) {
                slot = (headSlot - i + windowLengthInSlots) % windowLengthInSlots;
                updateSlot(obj, slot, consumption);
                return;
            }
        }

        throw new IllegalArgumentException("timestamp too old - no slot available");

    }

    public void updateSlot(T key, int slot, Float consumption) {
        AvgCalculator[] calculators = slottedAvgs.computeIfAbsent(key, k -> new AvgCalculator[windowLengthInSlots]);

        if (calculators[slot] == null)
            calculators[slot] = new AvgCalculator();

        calculators[slot].add(consumption);
    }

    /**
     * Obtain statistics for all key and it slides the window considering a new slot.
     *
     * @return statistics in the actual window of time
     * @see this#getAVgsSinceLastSlide() ()
     */
    public Map<T, Float> getAvgThenAdvanceWindow() {
        Map<T, Float> result = getAVgsSinceLastSlide();
        advanceWindow();
        return result;
    }

    public Float getTotalAvg() {

        AvgCalculator avgCalculator = new AvgCalculator();
        Map<T, Float> map = getAVgsSinceLastSlide();
        Set<T> keyset = map.keySet();

        for (T key : keyset)
            avgCalculator.add(map.get(key));

        return avgCalculator.getAvg();

    }

    /**
     * @return statistics in the actual window of time
     * @see this#computeTotalAvgExcludingCurrentSlot(Object)
     */
    public Map<T, Float> getAVgsSinceLastSlide() {
        Map<T, Float> result = new HashMap<>();
        for (T obj : slottedAvgs.keySet()) {
            result.put(obj, computeTotalAvgExcludingCurrentSlot(obj));
        }

        return result;
    }

    public Float computeTotalAvgExcludingCurrentSlot(T obj) {

        AvgCalculator[] curr = slottedAvgs.get(obj);
        int i;

        if (curr == null)
            throw new IllegalArgumentException("Object specified not found");

        Float sum = 0f;
        for (i = 0; i < windowLengthInSlots; i++) {
            AvgCalculator l = curr[i];
            if (l != null && i != headSlot) {
                sum += l.getAvg();
            }
        }

        return sum / (windowLengthInSlots - 1);
    }

    private void advanceWindow() {
        wipeSlot(tailSlot);
        advanceHead();
        lastSlide = LocalDateTime.now().truncatedTo(truncation);
    }

    public void wipeSlot(int slot) {
        for (T obj : slottedAvgs.keySet())
            resetSlotAvgToZero(obj, slot);
    }

    private void resetSlotAvgToZero(T obj, int slot) {
        AvgCalculator[] values = slottedAvgs.get(obj);
        if (values[slot] != null) {
            values[slot].setSum(0f);
            values[slot].setN(0);
        }
    }

    private void advanceHead() {
        headSlot = tailSlot;
        tailSlot = slotAfter(tailSlot);
    }

    private int slotAfter(int slot) {
        return (slot + 1) % windowLengthInSlots;
    }

    public void setWindowLengthInSlots(int windowLengthInSlots) {
        this.windowLengthInSlots = windowLengthInSlots;
    }

    public LocalDateTime getLastSlide() {
        return lastSlide;
    }

    public ChronoUnit getTruncation() {
        return truncation;
    }

    public void setTruncation(ChronoUnit truncation) {
        this.truncation = truncation;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

}
