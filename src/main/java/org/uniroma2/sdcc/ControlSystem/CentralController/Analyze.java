package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class Analyze extends BaseRichBolt {
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

    }

    @Override
    public void execute(Tuple tuple) {

        /* prende info dal monitor */
        /* prende dati sul traffico per strada */
        /* Confronta i dati e verifica se c'è bisogno di risettare il livello di luminosità della strada.
         * Se sì invia dati al plan

         */
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
