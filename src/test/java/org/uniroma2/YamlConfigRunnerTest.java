package org.uniroma2;

import org.junit.Test;
import org.uniroma2.sdcc.Utils.Configuration;
import org.uniroma2.sdcc.Utils.ServiceConfig;
import org.uniroma2.sdcc.Utils.YamlConfigRunner;

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
            configuration = yamlConfigRunner.getConfiguration("./config/statisticsConfig.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceConfig serviceConfig = configuration.getStatisticsTopologyParams();
        assertTrue(serviceConfig != null);
        assertTrue(serviceConfig.getTickTupleFrequency() != null);
        assertTrue(serviceConfig.getHourlyStatistics() != null);

    }
}
