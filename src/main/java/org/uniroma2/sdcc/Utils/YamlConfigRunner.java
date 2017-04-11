package org.uniroma2.sdcc.Utils;

/**
 * @author emanuele
 */

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigRunner {


    public Configuration getConfiguration(String filePath) throws IOException {


        Constructor constructor = new Constructor(Configuration.class);
        Yaml yaml = new Yaml(constructor);

        try (InputStream in = Files.newInputStream(Paths.get(filePath))) {
            Configuration config = yaml.loadAs(in, Configuration.class);
            System.out.println(config.toString());
            return config;
        }
    }

}
