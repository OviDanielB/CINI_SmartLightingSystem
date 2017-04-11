package org.uniroma2.sdcc.Utils;

import java.util.Map;

public class Configuration {
    ServiceConfig statisticsTopologyParams;

    public Configuration() {
    }

    @Override
    public String toString() {
        return "YamlConfig{" +
                "statistics=" + statisticsTopologyParams +
                '}';
    }

    public ServiceConfig getStatisticsTopologyParams() {
        return statisticsTopologyParams;
    }

    public void setStatisticsTopologyParams(ServiceConfig statisticsTopologyParams) {
        this.statisticsTopologyParams = statisticsTopologyParams;
    }
}

