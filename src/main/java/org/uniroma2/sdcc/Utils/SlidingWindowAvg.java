package org.uniroma2.sdcc.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Keep statistics on a specified window of time
 *
 * @param <T> key to which is associated a statistics value
 * @author Emanuele , Ovidiu , Laura , Marius
 * @see SlotBasedAvg
 */
public class SlidingWindowAvg<T> implements Serializable {

    private static final long serialVersionUID = -2645063988768785810L;

    private static final int WINDOWS_LENGTH_DEF = 24;
    private static final int SLOT_DURATION_IN_SECONDS_DEF = 60;

    private SlotBasedAvg<T> slotBasedAvg;          // Slot structure. Each slot keep statistics for an interval of time
    private int headSlot;                          // current slot
    private int tailSlot;                          // next slot to use
    private int windowLengthInSlots;               // window length in slot
    private int slotDurationInSeconds;             // slot duration in time
    private Long lastSlideInSeconds;               // instant of last slot change

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
        this.windowLengthInSlots = windowLengthInSlots;
        this.slotDurationInSeconds = slotDurationInSeconds;
        this.slotBasedAvg = new SlotBasedAvg<>(this.windowLengthInSlots);

        this.headSlot = 0;
        this.tailSlot = slotAfter(headSlot);
        this.lastSlideInSeconds = System.currentTimeMillis() / 1000;
    }

    /**
     * Update the data structure to obtain new statistics.
     *
     * @param obj         key
     * @param consumption quantity to consider in the statistics
     * @param timestamp   instant at which the consumption data are measured
     * @throws IllegalArgumentException timestamp's value in the future or too old that doesn't enters in the window
     */
    public void updatedConsumptionAvg(T obj, Float consumption, Long timestamp) throws IllegalArgumentException {

        Integer slot = -1, i;

        if (timestamp > (System.currentTimeMillis() / 1000))
            throw new IllegalArgumentException("The specified timestamp has a value in the future");

        while (timestamp > lastSlideInSeconds + slotDurationInSeconds)
            advanceWindow();

        // According to the timestamp, we found the right slot where consider the consumption value
        for (i = 0; i < windowLengthInSlots; i++) {

            if (timestamp >= lastSlideInSeconds - slotDurationInSeconds * i &&
                    timestamp <= lastSlideInSeconds + (1 - i) * slotDurationInSeconds) {
                slot = (headSlot - i + windowLengthInSlots) % windowLengthInSlots;
                break;
            }

        }

        if (slot == -1)
            throw new IllegalArgumentException("timestamp is too old");

        slotBasedAvg.updateConsumptionAvg(obj, slot, consumption);
    }

    /**
     * Obtain statistics for all key and it slides the window considering a new slot.
     *
     * @return statistics in the actual window of time
     * @see this#getAVgs()
     */
    public Map<T, Float> getAvgThenAdvanceWindow() {
        Map<T, Float> result = getAVgs();
        advanceWindow();
        return result;
    }

    private void advanceWindow() {
        slotBasedAvg.wipeSlot(tailSlot);
        advanceHead();
        lastSlideInSeconds = System.currentTimeMillis() / 1000;
    }

    /**
     * @return statistics in the actual window of time
     */
    public Map<T, Float> getAVgs() {
        Map<T, Float> result = new HashMap<>();
        for (T obj : slotBasedAvg.getSlottedAvgs().keySet()) {
            result.put(obj, slotBasedAvg.computeTotalAvg(obj));
        }

        return result;
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

    public Long getLastSlideInSeconds() {
        return lastSlideInSeconds;
    }

}
