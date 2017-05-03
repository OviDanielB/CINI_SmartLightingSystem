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
import org.uniroma2.sdcc.Utils.Cache.MemcachedManager;
import org.uniroma2.sdcc.Utils.JSONConverter;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Ranking Bolt
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GlobalRankBoltTest {

    private GlobalRankBolt bolt;

    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector outputCollector;

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning RankingBolt Test");

        MockitoAnnotations.initMocks(this);

        bolt = new GlobalRankBolt(4);
        bolt.prepare(new Config(), topologyContext, outputCollector);
    }


    /**
     * Test the main execute method with data tuple to compute current ranking;
     * functionalities tested elsewhere
     * @throws Exception
     */
    @Test
    public void T1_executeRankingCompute() throws Exception {

        Tuple tuple = mock(Tuple.class);
        when(tuple.getStringByField(Constants.RANKING)).thenReturn(
                "[{\"id\":1," +
                        "\"address\":" +
                        "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                        "\"lifetime\":{\"date\":" +
                        "{\"year\":2017,\"month\":12,\"day\":22}," +
                        "\"time\":" +
                        "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                        "}," +
                        "\"timestamp\":149203981028}," +
                        "{\"id\":2," +
                        "\"address\":" +
                        "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                        "\"lifetime\":{\"date\":" +
                        "{\"year\":2017,\"month\":12,\"day\":29}," +
                        "\"time\":" +
                        "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                        "}," +
                        "\"timestamp\":149203980928}]"
        );
        /* make false isTickTuple method */
        when(tuple.getSourceComponent()).thenReturn("_sy");
        when(tuple.getSourceStreamId()).thenReturn("_tick");

        bolt.execute(tuple);

        String expected_ranking_saved = tuple.getStringByField(Constants.RANKING);

        /* MemcachedManager tested elsewhere */
        assertEquals(expected_ranking_saved, bolt.cache.getString(MemcachedManager.CURRENT_GLOBAL_RANK));
    }


  /**
     * Test the main execute method with tick tuple to send last ranking;
     * functionalities tested elsewhere
     * @throws Exception
     */
    @Test
    public void T2_executeSending() throws Exception {

        /* MemcachedManager tested elsewhere */
        HashMap<Integer,Integer> counter = new HashMap<>();
        counter.put(1111,1);
        counter.put(2222, 1);
        bolt.cache.put(MemcachedManager.OLD_COUNTER, JSONConverter.fromHashMapIntInt(counter));

        Tuple tuple = mock(Tuple.class);
        /* make true isTickTuple method */
        when(tuple.getSourceComponent()).thenReturn("__system");
        when(tuple.getSourceStreamId()).thenReturn("__tick");

        bolt.execute(tuple);

        /* expected that the ranking saved at the test before is sent because is
        * the first to be sent */
        String expected_ranking_sent = bolt.cache.getString(MemcachedManager.CURRENT_GLOBAL_RANK);

        assertEquals(expected_ranking_sent, bolt.cache.getString(MemcachedManager.SENT_GLOBAL_RANKING));
    }

    /**
     * If no current ranking and no sent ranking have been previously saved,
     * ranking is updated.
     */
    @Test
    public void T3_rankingUpdated() {

        assertTrue(bolt.rankingUpdated("[{\"id\":1111," +
                "\"address\":" +
                "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                "\"lifetime\":{\"date\":" +
                "{\"year\":2017,\"month\":12,\"day\":22}," +
                "\"time\":" +
                "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                "}," +
                "\"timestamp\":149203981028}," +
                "{\"id\":2222," +
                "\"address\":" +
                "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                "\"lifetime\":{\"date\":" +
                "{\"year\":2017,\"month\":12,\"day\":29}," +
                "\"time\":" +
                "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                "}," +
                "\"timestamp\":149203980928}]"));
    }

    /**
     * If new ranking is equals to the  previously saved sent ranking,
     * ranking is not updated.
     */
    @Test
    public void T4_rankingNotUpdated() {

        assertFalse(bolt.rankingUpdated("[{\"id\":1111," +
                "\"address\":" +
                "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                "\"lifetime\":{\"date\":" +
                "{\"year\":2017,\"month\":12,\"day\":22}," +
                "\"time\":" +
                "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                "}," +
                "\"timestamp\":149203981028}," +
                "{\"id\":2222," +
                "\"address\":" +
                "{\"name\":\"VIA CAMBRIDGE\",\"number\":12,\"numberType\":\"CIVIC\"}," +
                "\"lifetime\":{\"date\":" +
                "{\"year\":2017,\"month\":12,\"day\":29}," +
                "\"time\":" +
                "{\"hour\":16,\"minute\":50,\"second\":50,\"nano\":489000000}" +
                "}," +
                "\"timestamp\":149203980928}]"));
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended RankingBolt Test");
    }
}