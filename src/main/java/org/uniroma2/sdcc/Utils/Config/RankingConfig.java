package org.uniroma2.sdcc.Utils.Config;

import java.io.Serializable;

/**
 * Configuration for ranking lamps parameters.
 */
public class RankingConfig implements Serializable {

    private static final long serialVersionUID = 43L;

    private Integer rank_size;
    private Integer lifetime_minimum;

    public RankingConfig() {
    }

    public Integer getRank_size() {
        return rank_size;
    }

    public void setRank_size(Integer rank_size) {
        this.rank_size = rank_size;
    }
    public Integer getLifetime_minimum() {
        return lifetime_minimum;
    }

    public void setLifetime_minimum(Integer lifetime_minimum) {
        this.lifetime_minimum = lifetime_minimum;
    }
}
