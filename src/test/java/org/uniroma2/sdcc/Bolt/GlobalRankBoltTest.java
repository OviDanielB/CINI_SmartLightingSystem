package org.uniroma2.sdcc.Bolt;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uniroma2.sdcc.Model.Address;
import org.uniroma2.sdcc.Model.AddressNumberType;
import org.uniroma2.sdcc.Utils.Ranking.OldestKRanking;
import org.uniroma2.sdcc.Utils.Ranking.RankLamp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test Ranking Bolt
 */
public class GlobalRankBoltTest {

    @Before
    public void setUp() throws Exception {
        System.out.println("[CINI] [TEST] Beginning RankingBolt Test");
    }

    /**
     * Test if ranking correctly updated if new tuple arrives and are included in
     * the ranking of the first 3 oldest lamps.
     */
    @Test
    public void Test1_rankingUpdated() {

        OldestKRanking current_list = new OldestKRanking(3);

        List<RankLamp> first_list = new ArrayList<>(3);
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime localDateTime = currentDate.toLocalDateTime();
        first_list.add(new RankLamp(1004, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(5), System.currentTimeMillis()));
        first_list.add(new RankLamp(1005, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(6), System.currentTimeMillis()));
        first_list.add(new RankLamp(1006, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(7), System.currentTimeMillis()));

		/* Update global rank */
        for (RankLamp lamp : first_list) {
            current_list.update(lamp);
        }

        List<RankLamp> new_list = new ArrayList<>(3);
        new_list.add(new RankLamp(1001, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(8), System.currentTimeMillis()));
        new_list.add(new RankLamp(1002, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(9), System.currentTimeMillis()));
        new_list.add(new RankLamp(1003, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(10), System.currentTimeMillis()));

		/* Update global rank */
        for (RankLamp lamp : new_list) {
            current_list.update(lamp);
        }

        List<RankLamp> globalOldestK = current_list.getOldestK();

        List<RankLamp> expected_ranking = new ArrayList<>();
        expected_ranking.add(0, new RankLamp(1003, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(10), System.currentTimeMillis()));
        expected_ranking.add(1, new RankLamp(1002, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(9), System.currentTimeMillis()));
        expected_ranking.add(2, new RankLamp(1001, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(8), System.currentTimeMillis()));

        for (int i = 0; i < expected_ranking.size(); i++) {
            assertEquals(expected_ranking.get(i).getId(), globalOldestK.get(i).getId());
        }
    }

    /**
     * Test if ranking correctly not updated if new tuple arrives and aren't included in
     * the ranking of the first 3 oldest lamps.
     */
    @Test
    public void Test2_rankingNotUpdated() {

        OldestKRanking current_list = new OldestKRanking(3);

        ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDateTime localDateTime = currentDate.toLocalDateTime();

        List<RankLamp> first_list = new ArrayList<>(3);
        first_list.add(new RankLamp(1004, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(8), System.currentTimeMillis()));
        first_list.add(new RankLamp(1005, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(9), System.currentTimeMillis()));
        first_list.add(new RankLamp(1006, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(10), System.currentTimeMillis()));

		/* Update global rank */
        for (RankLamp lamp : first_list) {
            current_list.update(lamp);
        }

        List<RankLamp> new_list = new ArrayList<>(3);
        new_list.add(new RankLamp(1001, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(5), System.currentTimeMillis()));
        new_list.add(new RankLamp(1002, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(6), System.currentTimeMillis()));
        new_list.add(new RankLamp(1003, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(7), System.currentTimeMillis()));

		/* Update global rank */
        boolean updated = false;
        for (RankLamp lamp : new_list) {
            updated |= current_list.update(lamp);
        }

        List<RankLamp> globalOldestK = current_list.getOldestK();


        List<RankLamp> expected_ranking = new ArrayList<>();
        expected_ranking.add(new RankLamp(1006, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(10), System.currentTimeMillis()));
        expected_ranking.add(new RankLamp(1005, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(9), System.currentTimeMillis()));
        expected_ranking.add(new RankLamp(1004, new Address("Via Cambridge", 12, AddressNumberType.CIVIC), localDateTime.minusDays(8), System.currentTimeMillis()));

        for (int i = 0; i < expected_ranking.size(); i++) {
            assertEquals(expected_ranking.get(i).getId(), globalOldestK.get(i).getId());
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("[CINI] [TEST] Ended RankingBolt Test");
    }
}