package org.uniroma2.sdcc.Utils.Config;

import java.util.Map;

/**
 * @author emanuele
 */
public class StatisticsBoltConfig {

    private Integer tickTupleFrequency;
    private Map<String, Integer> hourlyStatistics;
    private Map<String, Integer> dailyStatistics;

    public StatisticsBoltConfig() {
    }

    public Integer getTickTupleFrequency() {
        return tickTupleFrequency;
    }

    public void setTickTupleFrequency(Integer tickTupleFrequency) {
        this.tickTupleFrequency = tickTupleFrequency;
    }

    public Map<String, Integer> getHourlyStatistics() {
        return hourlyStatistics;
    }

    public void setHourlyStatistics(Map<String, Integer> hourlyStatistics) {
        this.hourlyStatistics = hourlyStatistics;
    }

    public Map<String, Integer> getDailyStatistics() {
        return dailyStatistics;
    }

    public void setDailyStatistics(Map<String, Integer> dailyStatistics) {
        this.dailyStatistics = dailyStatistics;
    }
}