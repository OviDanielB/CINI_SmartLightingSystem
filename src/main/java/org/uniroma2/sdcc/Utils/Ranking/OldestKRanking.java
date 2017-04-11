package org.uniroma2.sdcc.Utils.Ranking;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manage updates of the ranking list, keeping the sorting by lifetime value
 * of the first K RankLamp.
 */
public class OldestKRanking {

    private Comparator<RankLamp> comparator = null;
    private List<RankLamp> ranking = null;
    private int K;

    private static final int NOT_PRESENT = -1;

    public OldestKRanking(int k) {
        this.comparator = new RankLampComparator();
        this.ranking = new ArrayList<>();
        this.K = k;
    }

    /**
     * Update or insert a new RankLamp to rank, check if the update
     * changed the order of the first observed items or not.
     *
     * @param rankLamp lamp to rank
     * @return true if the ranking was updated, false if not
     */
    public boolean update(RankLamp rankLamp) {

        int sizePreUpdate = ranking.size();
        int oldPosition = findIndex(rankLamp);

        /* check if the lamp has just been ranked  */
        if (oldPosition != NOT_PRESENT) {
            /* remove previous ranking referring to the lamp    */
            ranking.remove(oldPosition);
        }

        int newPosition = add(rankLamp);

        int sizePostUpdate = ranking.size();

        if (newPosition == oldPosition &&
                sizePreUpdate == sizePostUpdate) {

			/* do not notify position changed */
            return false;

        } else if (newPosition > K - 1) {

			/* do not notify position changed in the lower side of the ranking */
            return false;
        }

        return true;

    }

    /**
     * Add a RankLamp object to the list of the ranked lamps,
     * returning the right position where insert rankLamp keeping
     * the sorting valid
     *
     * @param rankLamp new lamp to add to the ranking list
     * @return insertionPoint position
     */
    private int add(RankLamp rankLamp) {

        /* get item position in sorted list */
        int insertionPoint = Collections.binarySearch(ranking, rankLamp, comparator);
        /* (- insertionPoint) - 1 position where the key would be if not contained in the list */
        ranking.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, rankLamp);
        insertionPoint = (insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1;

        return insertionPoint;
    }

    /**
     * Search for the position index of the specified RankLamp.
     *
     * @param rankLamp lamp to search
     * @return position index if found, -1 otherwise
     */
    private int findIndex(RankLamp rankLamp) {

        int i = 0;
        for (RankLamp l : ranking) {
            if (l.getId() == rankLamp.getId()) {
                return i;
            }
            i++;
        }

        return NOT_PRESENT;
    }

    /**
     * Get global ranking list composed by maximum K values.
     *
     * @return ranking list
     */
    public List<RankLamp> getOldestK(){

        List<RankLamp> old = new ArrayList<>();

        if (!ranking.isEmpty()){
            int items = Math.min(K, ranking.size());

            for (int i = 0; i < items; i++){
                old.add(ranking.get(i));
            }
        }

        return old;
    }

    @Override
    public String toString() {
        return ranking.toString();
    }

}
