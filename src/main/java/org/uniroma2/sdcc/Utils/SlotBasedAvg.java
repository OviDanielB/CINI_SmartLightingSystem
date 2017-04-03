package org.uniroma2.sdcc.Utils;

import java.io.Serializable;
import java.util.*;

/**
 * This class provides per-slot counts of the occurrences of objects.
 * It can be used, for instance, as a building block for implementing sliding window counting of objects.
 *
 * @param <T> The type of those objects we want to count.
 */
public class SlotBasedAvg<T> implements Serializable {

    private static final long serialVersionUID = 4858185737378394432L;

    private final Map<T, AvgCalculator[]> slottedAvgs = new HashMap<>();
    private final int numSlots;

    public SlotBasedAvg(int numSlots) {
        if (numSlots <= 0) {
            throw new IllegalArgumentException("Number of slots must be greater than zero (you requested " + numSlots
                    + ")");
        }
        this.numSlots = numSlots;
    }

    public void updateConsumptionAvg(T key, int slot, Float consumption) {
        AvgCalculator[] calculators = slottedAvgs.computeIfAbsent(key, k -> new AvgCalculator[numSlots]);

        if (calculators[slot] == null)
            calculators[slot] = new AvgCalculator();

        calculators[slot].add(consumption);
    }


    public Float computeTotalAvg(T obj) {
        // obtain the calculator of the average value for each slot
        AvgCalculator[] curr = slottedAvgs.get(obj);

        if (curr == null)
            throw new IllegalArgumentException("Object specified not found");

        Float totalAvg = 0f;
        Integer n = 0;
        // sum all average value in each slot.
        for (AvgCalculator l : curr) {
            if (l != null) {
                n++;
                totalAvg += l.getAvg(); // get the slot average value and sum it to the total
            }
        }

        assert n <= numSlots;

        // divide the sum of average value of the slots for the slot number
        // obtain average value on whole window length
        return totalAvg / n;
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

    public Map<T, AvgCalculator[]> getSlottedAvgs() {
        return slottedAvgs;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getNumSlots() {
        return numSlots;
    }
}
