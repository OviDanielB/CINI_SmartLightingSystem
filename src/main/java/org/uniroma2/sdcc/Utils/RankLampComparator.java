package org.uniroma2.sdcc.Utils;

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

        int diff = (int) lamp1.getLifetime().getTime() - (int) lamp2.getLifetime().getTime();

        if ( diff == 0 ) {
            return (int) ( lamp1.getTimestamp().getTime() - lamp2.getTimestamp().getTime());
        }

        return diff;
    }
}
