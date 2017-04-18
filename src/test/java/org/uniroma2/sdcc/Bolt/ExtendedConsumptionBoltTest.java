package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.ExtendendIndividualConsumptionBolt;
import tools.MockTupleHelpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author emanuele
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendedConsumptionBoltTest {

    private final static int WINDOW_LENGTH = 60;
    private final static int EMIT_FREQUENCY = 5;

    @Test
    public void extendedBoltMustUpdateCounter() {

        int prod_factor = 4;

        ExtendendIndividualConsumptionBolt bolt = new ExtendendIndividualConsumptionBolt(WINDOW_LENGTH,
                EMIT_FREQUENCY * prod_factor, EMIT_FREQUENCY);

        Map conf = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        bolt.prepare(conf, context, collector);

        Tuple tickTuple = MockTupleHelpers.mockTickTuple();
        int count = bolt.getTickCount();

        bolt.execute(tickTuple);
        count += 1;
        assertTrue(bolt.getTickCount() == count % prod_factor);

        bolt.execute(tickTuple);
        count += 1;
        assertTrue(bolt.getTickCount() == count % prod_factor);
    }

    @Test
    public void isValidMethodTest() {

        String iso8601 = "2016-02-14T18:32:00.150Z";
        ZonedDateTime zdt = ZonedDateTime.parse(iso8601);
        LocalDateTime ldt = zdt.toLocalDateTime();
        ldt = ldt.truncatedTo(ChronoUnit.SECONDS);

        ExtendendIndividualConsumptionBolt bolt = new ExtendendIndividualConsumptionBolt(WINDOW_LENGTH,
                EMIT_FREQUENCY*2, EMIT_FREQUENCY);
        Method method = null;
        try {
            method = ExtendendIndividualConsumptionBolt.class.getDeclaredMethod("isValid", LocalDateTime.class);
            method.setAccessible(true);
            assertTrue(method.invoke(bolt, ldt).equals(true));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ifNotValidDateIsValidMustReturnFalse() {

        String iso8601 = "2016-02-14T18:32:01.150Z";
        ZonedDateTime zdt = ZonedDateTime.parse(iso8601);
        LocalDateTime ldt = zdt.toLocalDateTime();

        Method method = null;

        ExtendendIndividualConsumptionBolt bolt = new ExtendendIndividualConsumptionBolt(WINDOW_LENGTH,
                EMIT_FREQUENCY*2, EMIT_FREQUENCY);

        try {
            method = ExtendendIndividualConsumptionBolt.class.getDeclaredMethod("isValid", LocalDateTime.class);
            method.setAccessible(true);
            assertTrue(method.invoke(bolt, ldt).equals(false));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
