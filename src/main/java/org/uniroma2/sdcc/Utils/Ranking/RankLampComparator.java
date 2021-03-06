package org.uniroma2.sdcc.Utils.Ranking;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;

/**
 * Comparator on which is based the sorting of RankLamp objects
 * that compare lifetime values of a couple of objects.
 * If it's the same value of lifetime, sorting is based on the
 * timestamp of the data.
 * */
public class RankLampComparator implements Comparator<RankLamp> {

    @Override
    public int compare(RankLamp lamp1, RankLamp lamp2) {

        long diff = ChronoUnit.MILLIS.between(lamp2.getLifetime(),lamp1.getLifetime());

        if(diff == 0){
            diff = lamp2.getTimestamp() - lamp1.getTimestamp();
        }

        return (int) diff;
    }
}
