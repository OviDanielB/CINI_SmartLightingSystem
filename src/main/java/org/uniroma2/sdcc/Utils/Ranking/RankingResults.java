package org.uniroma2.sdcc.Utils.Ranking;

import java.util.List;

/**
 * Message format to sent to RabbitMq queue containing :
 * - ranking of the oldest K lamp replacements
 * - number of all lamps which was replaced more than LIFETIME_THRESHOLD ago
 */
public class RankingResults {

    List<RankLamp> ranking;
    int count;

    public RankingResults(List<RankLamp> ranking, int count) {
        this.ranking = ranking;
        this.count = count;
    }

    public List<RankLamp> getRanking() {
        return ranking;
    }

    public void setRanking(List<RankLamp> ranking) {
        this.ranking = ranking;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean equals(RankingResults rankingResults) {
        List<RankLamp> ranking = this.getRanking();

        return ranking.stream().filter(t -> {
                    Integer index = ranking.indexOf(t);
                    return t.equals(rankingResults.getRanking().get(index));
                }).count() == ranking.size()
                && this.getCount() == rankingResults.getCount();
    }
}
