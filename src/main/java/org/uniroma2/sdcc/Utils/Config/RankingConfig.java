package org.uniroma2.sdcc.Utils.Config;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Configuration for ranking lamps parameters.
 */
public class RankingConfig implements Serializable {

    private static final long serialVersionUID = 42L;

    private Integer rank_size;
    private LocalDateTime lifetime_minimum;

    public RankingConfig() {
    }

    public Integer getRankSize() {
        return rank_size;
    }

    public void setRankSize(Integer rank_size) {
        this.rank_size = rank_size;
    }

    public LocalDateTime getLifetime_minimum() {
        return lifetime_minimum;
    }

    public void setLifetime_minimum(LocalDateTime lifetime_minimum) {
        this.lifetime_minimum = lifetime_minimum;
    }
}
