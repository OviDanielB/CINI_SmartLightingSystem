package org.uniroma2.sdcc.utils;

import org.junit.Test;
import org.uniroma2.sdcc.Utils.Config.Configuration;
import org.uniroma2.sdcc.Utils.Config.ServiceConfig;
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
            YamlConfigRunner yamlConfigRunner = new YamlConfigRunner("./config/config.yml");
            configuration = yamlConfigRunner.getConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceConfig serviceConfig = configuration.getStatisticsTopologyParams();
        assertTrue(serviceConfig != null);
        assertTrue(serviceConfig.getTickTupleFrequency() != null);
        assertTrue(serviceConfig.getHourlyStatistics() != null);

    }
}
