package org.uniroma2.sdcc.Utils.Config;

/**
 * @author emanuele
 */

import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigRunner {

    private final static String configFile = "config.yml";

    public Configuration getConfiguration() throws IOException {

        Constructor constructor = new Constructor(Configuration.class);
        Yaml yaml = new Yaml(constructor);

        InputStream in = ClassLoader.getSystemResourceAsStream(configFile);
        Configuration config = yaml.loadAs(in, Configuration.class);
        System.out.println(config.toString());
        return config;
    }

}

