package org.uniroma2.sdcc.Utils.Config;

import java.util.List;

public class Configuration {
    private String project_name;
    private String version;
    private List<String> group_members;
    private ServiceConfig statisticsTopologyParams;
    private RabbitConfig queue_out;
    private RankingConfig rankingTopologyParams;
    private ControlConfig controlThresholds;

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

    public RabbitConfig getQueue_out() {
        return queue_out;
    }

    public void setQueue_out(RabbitConfig queue_out) {
        this.queue_out = queue_out;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getGroup_members() {
        return group_members;
    }

    public void setGroup_members(List<String> group_members) {
        this.group_members = group_members;
    }

    public RankingConfig getRankingTopologyParams() {
        return rankingTopologyParams;
    }

    public void setRankingTopologyParams(RankingConfig rankingTopologyParams) {
        this.rankingTopologyParams = rankingTopologyParams;
    }

    public ControlConfig getControlThresholds() {
        return controlThresholds;
    }

    public void setControlThresholds(ControlConfig controlConfig) {
        this.controlThresholds = controlConfig;
    }
}

