package org.uniroma2.sdcc.Utils;

import com.google.gson.Gson;
import org.junit.Test;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

import static java.time.ZoneOffset.UTC;
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

        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime ld = currentDate.toLocalDateTime();
        LocalDateTime localDateTime = ld.minus(SLOT_DURATION_IN_SECONDS - 1, ChronoUnit.SECONDS);
        slidingWindowAvg.updatedConsumptionAvg(3, 12f, localDateTime);

        Map<Integer, Float> result = slidingWindowAvg.getAVgsSinceLastSlide();

        assertTrue(result.containsKey(3));
        assertEquals(result.get(3), 12f, 0.01);
    }

    @Test
    public void windowMustGenerateRightAvg() {

        SlidingWindowAvg<Integer> slidingWindowAvg = new SlidingWindowAvg<>(WINDOW_LENGTH_IN_SLOT,
                SLOT_DURATION_IN_SECONDS);
        Random random = new Random(System.currentTimeMillis());
        Float value;

        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime ld = currentDate.toLocalDateTime();
        LocalDateTime localDateTime = ld.minus(SLOT_DURATION_IN_SECONDS - 1, ChronoUnit.SECONDS);
        slidingWindowAvg.updatedConsumptionAvg(3, 12f, localDateTime);

        for (int i = 0; i < 100; i++) {
            value = (float) random.nextGaussian() * GAUSSIAN_STDEV + GAUSSIAN_MEAN;
            slidingWindowAvg.updatedConsumptionAvg(1, value, localDateTime);
        }

        Map<Integer, Float> result = slidingWindowAvg.getAVgsSinceLastSlide();
        assertTrue(result.containsKey(1));
        assertEquals(result.get(1), GAUSSIAN_MEAN, GAUSSIAN_STDEV);
    }

    @Test
    public void ZonedTimestampTest() {

        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime localDateTime = currentDate.toLocalDateTime();
        Gson gson = new Gson();
        System.out.print(gson.toJson(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()) + '\n');

        LocalDateTime localDateTime1 = LocalDateTime.now();
        System.out.print(gson.toJson(localDateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) + '\n');
        System.out.print(gson.toJson(System.currentTimeMillis()) + '\n');

        Long timestamp = System.currentTimeMillis();
        LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), UTC);
        System.out.print(gson.toJson(ts) + '\n');
    }

}
