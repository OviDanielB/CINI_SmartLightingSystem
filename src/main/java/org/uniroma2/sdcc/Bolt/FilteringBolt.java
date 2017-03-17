package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.StreetLamp;
import org.uniroma2.sdcc.Model.StreetLampMessage;

import java.util.Map;

public class FilteringBolt extends BaseRichBolt {

    private OutputCollector collector;

    public FilteringBolt() {
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }


    /*
    *   CINI Data Format:
    *
    *   1   id                          32 bit street-lamp identifier
    *   2   address                     street-lamp location (es Via/Piazza - km/civico -)
    *   3   on ( state on/off )         state
    *   4   consumption                 32 bit value representing energy consumption in Watt
    *   5   intensity                   percentage of the maximum intensity
    *   6   lifetime                    date
    *   7   naturalLightLevel           level of the measured natural light intensity
    *   8   timestamp                   32 bit value
    *
    */

    @Override
    public void execute(Tuple tuple) {

        String json = (String) tuple.getValueByField("line");

        Gson gson = new Gson();
//      JSON to Java object, read it from a Json String.
        StreetLampMessage msg = gson.fromJson(json, StreetLampMessage.class);
        StreetLamp streetLamp = msg.getStreetLamp();

        Integer id = streetLamp.getID();
        String address = streetLamp.getAddress().toString();
        Boolean on = streetLamp.isOn();
        String model = streetLamp.getLampModel().toString();
        Float consumption = streetLamp.getConsumption();
        Float intensity = streetLamp.getLightIntensity();
        String date = streetLamp.getLifetime();
        Float naturalLight = msg.getNaturalLight().getLevel();
        String timestamp = msg.getTimestamp().toString();

        Values values = new Values();
        values.add(id);
        values.add(address);
        values.add(on);
        values.add(model);
        values.add(consumption);
        values.add(intensity);
        values.add(date);
        values.add(naturalLight);
        values.add(timestamp);

        collector.emit(values);
        collector.ack(tuple);

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("id", "address", "on", "model", "consumption", "intensity",
                "lifetime", "naturalLightLevel", "timestamp"));
    }
}
