package org.uniroma2.sdcc.Bolt;

import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AddressNumberType;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Ranking Bolt
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PartialRankBoltTest {

    private PartialRankBolt bolt;

    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector outputCollector;

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning PartialRankingBolt Test");

        MockitoAnnotations.initMocks(this);

        bolt = new PartialRankBolt(3);
        bolt.prepare(new Config(), topologyContext, outputCollector);
    }

    /**
     * Test the main execute method to compute current partial ranking;
     * functionalities tested elsewhere
     * @throws Exception
     */
    @Test
    public void T1_execute() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getValueByField(Constants.ID)).thenReturn(1001);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(20, ChronoUnit.DAYS));
        when(tuple.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        bolt.execute(tuple);
    }

    /**
     * Check if after receiving a tuple, when an equals one arrives ranking results
     * as not updated.
     */
    @Test
    public void T2_notUpdatedRanking() {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getValueByField(Constants.ID)).thenReturn(1001);
        when(tuple.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(20, ChronoUnit.DAYS));
        when(tuple.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        bolt.execute(tuple);

        assertFalse(bolt.updateRanking(tuple));
    }

    /**
     * Check if after receiving a tuple, when a new one arrives ranking results as
     * updated.
     */
    @Test
    public void T3_updatedRanking() {
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.getValueByField(Constants.ID)).thenReturn(1001);
        when(tuple1.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple1.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(20, ChronoUnit.DAYS));
        when(tuple1.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        Tuple tuple2 = mock(Tuple.class);
        when(tuple2.getValueByField(Constants.ID)).thenReturn(2002);
        when(tuple2.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple2.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(22, ChronoUnit.DAYS));
        when(tuple2.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        bolt.execute(tuple1);

        assertTrue(bolt.updateRanking(tuple2));
    }

    /**
     * Check if correct ranking of size K = 3 is computed, even if more
     * than 3 tuple coming from previous bolt.
     */
    @Test
    public void T4_correctRanking() {
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.getValueByField(Constants.ID)).thenReturn(2002);
        when(tuple1.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple1.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(19, ChronoUnit.DAYS));
        when(tuple1.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        Tuple tuple2 = mock(Tuple.class);
        when(tuple2.getValueByField(Constants.ID)).thenReturn(3003);
        when(tuple2.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple2.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(18, ChronoUnit.DAYS));
        when(tuple2.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        Tuple tuple3 = mock(Tuple.class);
        when(tuple3.getValueByField(Constants.ID)).thenReturn(1001);
        when(tuple3.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple3.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(20, ChronoUnit.DAYS));
        when(tuple3.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        Tuple tuple4 = mock(Tuple.class);
        when(tuple4.getValueByField(Constants.ID)).thenReturn(4004);
        when(tuple4.getValueByField(Constants.ADDRESS)).thenReturn(new Address("VIA CAMBRIDGE", 12, AddressNumberType.CIVIC));
        when(tuple4.getValueByField(Constants.LIFETIME)).thenReturn(LocalDateTime.now().minus(8, ChronoUnit.DAYS));
        when(tuple4.getValueByField(Constants.TIMESTAMP)).thenReturn(Long.getLong("149203981028"));

        bolt.execute(tuple1);
        bolt.execute(tuple2);
        bolt.execute(tuple3);
        bolt.execute(tuple4);

        List<RankLamp> ranking = bolt.ranking.getOldestK();

        List<RankLamp> expected_ranking = new ArrayList<>(3);
        expected_ranking.add(new RankLamp(
                (Integer) tuple3.getValueByField(Constants.ID),
                (Address) tuple3.getValueByField(Constants.ADDRESS),
                (LocalDateTime) tuple3.getValueByField(Constants.LIFETIME),
                (Long) tuple3.getValueByField(Constants.TIMESTAMP)));
        expected_ranking.add(new RankLamp(
                (Integer) tuple1.getValueByField(Constants.ID),
                (Address) tuple1.getValueByField(Constants.ADDRESS),
                (LocalDateTime) tuple1.getValueByField(Constants.LIFETIME),
                (Long) tuple1.getValueByField(Constants.TIMESTAMP)));
        expected_ranking.add(new RankLamp(
                (Integer) tuple2.getValueByField(Constants.ID),
                (Address) tuple2.getValueByField(Constants.ADDRESS),
                (LocalDateTime) tuple2.getValueByField(Constants.LIFETIME),
                (Long) tuple2.getValueByField(Constants.TIMESTAMP)));

        assertFalse(expected_ranking.stream().filter(r -> {
            Integer index = expected_ranking.indexOf(r);
            return r.getId() != ranking.get(index).getId();
        }).count() > 0);
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended PartialRankingBolt Test");
    }
}