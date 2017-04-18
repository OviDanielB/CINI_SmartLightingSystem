package org.uniroma2.sdcc.Utils.Config;

/**
 * @author emanuele
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.uniroma2.sdcc.Utils.Config.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigRunner {

    private String filePath;

    public YamlConfigRunner(String filePath) {
        this.filePath = filePath;
    }

    public Configuration getConfiguration() throws IOException {

        Constructor constructor = new Constructor(Configuration.class);
        Yaml yaml = new Yaml(constructor);

        InputStream in = Files.newInputStream(Paths.get(filePath));
        Configuration config = yaml.loadAs(in, Configuration.class);
        System.out.println(config.toString());
        return config;
    }
}

