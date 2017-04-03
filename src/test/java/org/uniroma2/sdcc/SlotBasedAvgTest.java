package org.uniroma2.sdcc;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.uniroma2.sdcc.Utils.SlotBasedAvg;

import java.util.Random;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SlotBasedAvgTest {

    private static final int ANY_NUM_SLOTS = 1;
    private static final int ANY_SLOT = 0;
    private static final Object ANY_OBJECT = "ANY_OBJECT";

    @DataProvider
    public Object[][] legalConsumptionValue() {
        return new Object[][]{{12.4f}, {0}, {3f}, {4}, {19}};
    }

    @DataProvider
    public Object[][] illegalNumSlotsData() {
        return new Object[][]{{-10}, {-3}, {-2}, {-1}, {0}};
    }

    @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "illegalNumSlotsData")
    public void negativeOrZeroNumSlotsShouldThrowIAE(int numSlots) {
        new SlotBasedAvg<>(numSlots);
    }

    @DataProvider
    public Object[][] legalNumSlotsData() {
        return new Object[][]{{1}, {2}, {3}, {20}};
    }

    @Test(dataProvider = "legalNumSlotsData")
    public void positiveNumSlotsShouldBeOk(int numSlots) {
        new SlotBasedAvg<>(numSlots);
    }

    @Test
    public void newInstanceShouldHaveAvgZero() {
        // given
        SlotBasedAvg<Object> counter = new SlotBasedAvg<>(ANY_NUM_SLOTS);

        counter.updateConsumptionAvg(ANY_OBJECT, 0, 0f);
        // when
        Float avgs = counter.computeTotalAvg(ANY_OBJECT);

        // then
        assertEquals(avgs, 0f);
    }

    @DataProvider
    public Object[][] consumptionData() {
        return new Object[][]{{2.0f}, {4.3f}, {4.0f}};
    }

    @Test(dataProvider = "consumptionData")
    public void shouldReturnNonEmptyCountsWhenAtLeastOneObjectWasCounted(Float consumption) {
        // given
        SlotBasedAvg<Object> counter = new SlotBasedAvg<>(ANY_NUM_SLOTS);
        counter.updateConsumptionAvg(ANY_OBJECT, ANY_SLOT, consumption);

        // when
        Float avg = counter.computeTotalAvg(ANY_OBJECT);

        // then
        assertNotEquals(avg, 0f);
    }

    @DataProvider
    public Object[][] updateAvgData() {
        return new Object[][]{{new String[]{"first", "second"}, new Float[]{2f, 6f}, new Float[][]{{2f, 2f, 2f}, {5f, 7f, 6f}}}};
    }


    @Test(dataProvider = "updateAvgData")
    public void shouldUpdateAvg(Object[] objects, Float[] expAvg, Float[][] cons) {

        // given
        SlotBasedAvg<Object> counter = new SlotBasedAvg<>(ANY_NUM_SLOTS);

        // when
        for (int i = 0; i < objects.length; i++) {
            Object obj = objects[i];
            Float[] consValue = cons[i];
            for (Float aConsValue : consValue) {
                counter.updateConsumptionAvg(obj, ANY_SLOT, aConsValue);
            }
        }

        // then
        for (int i = 0; i < objects.length; i++)
            assertEquals(counter.computeTotalAvg(objects[i]), expAvg[i]);
    }

    @Test
    public void getAVgstest() {

        int i, j, iteration = 100;
        Float value = 2f;

        SlotBasedAvg<Integer> slotBasedAvg = new SlotBasedAvg<>(24);
        Random rand = new Random(System.currentTimeMillis());

        assertTrue(slotBasedAvg.getNumSlots() == 24);

        for (j = 0; j < 10; j++) {
            for (i = 0; i < iteration; i++) {
                slotBasedAvg.updateConsumptionAvg(j, 0, value);
            }
        }

        Float result = slotBasedAvg.computeTotalAvg(0);

        assertEquals(result, value, 0.01f);
    }

}
