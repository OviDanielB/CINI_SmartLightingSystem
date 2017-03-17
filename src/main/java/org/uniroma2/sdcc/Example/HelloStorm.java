package org.uniroma2.sdcc.Example;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.uniroma2.sdcc.Spouts.RabbitMQSpout;


/**
 * Created by ovidiudanielbarba on 07/03/2017.
 */
public class HelloStorm {

    public static void main(String[] args) throws Exception{
        Config config = new Config();
        //config.setDebug(true);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        TopologyBuilder builder = new TopologyBuilder();
        //builder.setSpout("line-reader-spout", new LineReaderSpout());
        builder.setSpout("line-reader-spout",new RabbitMQSpout());
        builder.setBolt("word-spitter", new WordSpitterBolt()).shuffleGrouping("line-reader-spout");
        builder.setBolt("word-counter", new WordCounterBolt()).shuffleGrouping("word-spitter");


        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("HelloStorm", config, builder.createTopology());

        Thread.sleep(60000);

        cluster.shutdown();


        /*
        Yaml yaml = new Yaml();
        Object v = yaml.load(new FileInputStream(new File("config/config.yml")));
        System.out.println(v.toString());

        */

        //StormSubmitter.submitTopology("Hello Storm",config,builder.createTopology());


    }



}