package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Model.StreetLamp;
import org.uniroma2.sdcc.Model.StreetLampMessage;

import java.util.Date;
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

        String json = (String) tuple.getValueByField(StreetLampMessage.JSON_STRING);

        StreetLampMessage streetLamp;
        try {
            Gson gson = new Gson();
            /* JSON to Java object, read it from a Json String. */
            streetLamp = gson.fromJson(json, StreetLampMessage.class);

        } catch (JsonParseException e){
            /* wrong json format */
            e.printStackTrace();
            collector.ack(tuple);
            return;
        }

        emitValidLampTuple(tuple,streetLamp);

        /*
        Integer id = data.getID();
        String address = data.getAddress().toString();
        Boolean on = data.isOn();
        String model = data.getLampModel().toString();
        Float consumption = data.getConsumption();
        Float intensity = data.getLightIntensity();
        Date date = data.getLifetime();
        String timestamp = data.toString();

        Values values = new Values();
        values.add(id);
        values.add(address);
        values.add(on);
        values.add(model);
        values.add(consumption);
        values.add(intensity);
        values.add(date);
        values.add(timestamp);

        collector.emit(values);
        collector.ack(tuple);

        */
    }

    /**
     * check and emit only valid tuples
     * @param tuple received from spout tuple
     * @param streetLamp parsed from tuple
     */
    private void emitValidLampTuple(Tuple tuple, StreetLampMessage streetLamp) {

        if(validStreetLampFormat(streetLamp)) {

            /* anchor tuple to new streetLamp value */
            collector.emit(tuple, new Values(streetLamp));
            collector.ack(tuple);

        } else {
            collector.ack(tuple);
        }
    }

    /**
     * the street light should have a valid format
     * ex: intensity and naturalLight level should be percentages,
     *     timestamp should not be too far in the past, etc
     * @param streetLamp needed validation
     * @return true if valid, false otherwise
     */
    private boolean validStreetLampFormat(StreetLampMessage streetLamp) {
        // TODO check if 1st field is ID,2nd is an address, etc
        return true;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        /*
        outputFieldsDeclarer.declare(new Fields(StreetLampMessage.ID, StreetLampMessage.ADDRESS,
                StreetLampMessage.ON, StreetLampMessage.LAMP_MODEL, StreetLampMessage.CONSUMPTION,
                StreetLampMessage.INTENSITY, StreetLampMessage.LIFETIME,
                StreetLampMessage.NATURAL_LIGHT_LEVEL, StreetLampMessage.TIMESTAMP));

                */

        outputFieldsDeclarer.declare(new Fields(StreetLampMessage.STREET_LAMP_MSG));
    }
}