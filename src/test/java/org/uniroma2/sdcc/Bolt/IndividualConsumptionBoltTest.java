package org.uniroma2.sdcc.Bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.uniroma2.sdcc.Bolt.ConsumptionStatisticsBolt.IndividualConsumptionBolt;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AddressNumberType;
import tools.MockTupleHelpers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author emanuele
 */
@RunWith(MockitoJUnitRunner.class)
public class IndividualConsumptionBoltTest {

    private final static int WINDOW_LENGTH = 60;
    private final static int EMIT_FREQUENCY = 5;

    private static final String ANY_NON_SYSTEM_COMPONENT_ID = "irrelevant_component_id";
    private static final String ANY_NON_SYSTEM_STREAM_ID = "irrelevant_stream_id";

    private Tuple mockNormalTuple(Object obj) {
        Tuple tuple = MockTupleHelpers.mockTuple(ANY_NON_SYSTEM_COMPONENT_ID, ANY_NON_SYSTEM_STREAM_ID);
        when(tuple.getValue(0)).thenReturn(obj);
        return tuple;
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldEmitNothingIfNoObjectHasBeenCountedYetAndTickTupleIsReceived() {
        // given
        Tuple tickTuple = MockTupleHelpers.mockTickTuple();
        IndividualConsumptionBolt bolt = new IndividualConsumptionBolt(WINDOW_LENGTH, EMIT_FREQUENCY);
        Map conf = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        bolt.prepare(conf, context, collector);

        // when
        bolt.execute(tickTuple);

        // then
        verifyZeroInteractions(collector);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldUpdateStatistics() {

        // given
        Tuple normalTuple = mockNormalTuple(new Object());
        Tuple tickTuple = MockTupleHelpers.mockTickTuple();

        IndividualConsumptionBolt bolt = new IndividualConsumptionBolt(WINDOW_LENGTH, EMIT_FREQUENCY);
        Map conf = mock(Map.class);
        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);
        bolt.prepare(conf, context, collector);

        when(normalTuple.getIntegerByField("id")).thenReturn(1);
        when(normalTuple.getFloatByField("consumption")).thenReturn(12.4f);
        Address address = new Address("Via del politecnico", 12, AddressNumberType.CIVIC);
        when(normalTuple.getValueByField("street")).thenReturn(address);

        Long timestamp = System.currentTimeMillis() - (EMIT_FREQUENCY / 2);
        LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), UTC);
        when(normalTuple.getValueByField("timestamp")).thenReturn(ts);


        // when
        bolt.execute(normalTuple);
        bolt.execute(tickTuple);

        // then
        verify(collector).emit(any(Values.class));
    }

    @Test
    public void shouldDeclareOutputFields() {
        // given
        OutputFieldsDeclarer declarer = mock(OutputFieldsDeclarer.class);
        IndividualConsumptionBolt bolt = new IndividualConsumptionBolt(WINDOW_LENGTH, EMIT_FREQUENCY);

        // when
        bolt.declareOutputFields(declarer);

        // then
        verify(declarer, times(1)).declare(any(Fields.class));
    }

}
