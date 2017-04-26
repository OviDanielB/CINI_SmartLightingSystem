package org.uniroma2.sdcc.utils;

import org.junit.Test;
import org.uniroma2.sdcc.Utils.Config.Configuration;
import org.uniroma2.sdcc.Utils.Config.StatisticsBoltConfig;
import org.uniroma2.sdcc.Utils.Config.YamlConfigRunner;

import java.io.IOException;
import static org.junit.Assert.assertTrue;

/**
 * @author emanuele
 */
public class YamlConfigRunnerTest {

    @Test
    public void yamlConfigRunnerMustUploadCondif() {

        Configuration configuration = null;
        try {
            YamlConfigRunner yamlConfigRunner = new YamlConfigRunner();
            configuration = yamlConfigRunner.getConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatisticsBoltConfig statisticsBoltConfig = configuration.getStatisticsTopologyParams();
        assertTrue(statisticsBoltConfig != null);
        assertTrue(statisticsBoltConfig.getTickTupleFrequency() != null);
        assertTrue(statisticsBoltConfig.getHourlyStatistics() != null);

    }
}
