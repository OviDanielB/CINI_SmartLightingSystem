package org.uniroma2;

import org.junit.Test;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author emanuele
 */
public class SlidingWindowTest {

    private final static int WINDOW_LENGTH_IN_SLOT = 10;
    private final static int SLOT_DURATION_IN_SECONDS = 10;

    private final static Float GAUSSIAN_MEAN = 60f;
    private final static Float GAUSSIAN_STDEV = 15f;

    @Test
    public void windowMustBeUpdated() {
        SlidingWindowAvg<Integer> slidingWindowAvg = new SlidingWindowAvg<>(WINDOW_LENGTH_IN_SLOT,
                SLOT_DURATION_IN_SECONDS);

        LocalDateTime localDateTime = LocalDateTime.now().minus(SLOT_DURATION_IN_SECONDS - 1, ChronoUnit.SECONDS);
        slidingWindowAvg.updatedConsumptionAvg(3, 12f, localDateTime);

        Map<Integer, Float> result = slidingWindowAvg.getAVgsSinceLastSlide();

        assertTrue(result.containsKey(3));
        assertEquals(result.get(3), 12f / 10, 0.01);
    }

    @Test
    public void windowMustGenerateRightAvg() {

        SlidingWindowAvg<Integer> slidingWindowAvg = new SlidingWindowAvg<>(WINDOW_LENGTH_IN_SLOT,
                SLOT_DURATION_IN_SECONDS);
        Random random = new Random(System.currentTimeMillis());
        Float value;

        LocalDateTime localDateTime = LocalDateTime.now().minus(SLOT_DURATION_IN_SECONDS - 1, ChronoUnit.SECONDS);
        slidingWindowAvg.updatedConsumptionAvg(3, 12f, localDateTime);

        for (int i = 0; i < 100; i++) {
            value = (float) random.nextGaussian() * GAUSSIAN_STDEV + GAUSSIAN_MEAN;
            slidingWindowAvg.updatedConsumptionAvg(1, value, localDateTime);
        }

        Map<Integer, Float> result = slidingWindowAvg.getAVgsSinceLastSlide();
        assertTrue(result.containsKey(1));
        assertEquals(result.get(1), GAUSSIAN_MEAN / WINDOW_LENGTH_IN_SLOT, 1);

    }

}
