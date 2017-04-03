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
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.*;
import org.uniroma2.sdcc.Utils.TupleHelpers;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

        if (!TupleHelpers.isTickTuple(tuple)) {

            String json = (String) tuple.getValueByField(Constant.JSON_STRING);

            StreetLampMessage streetLampMessage;
            try {
                Gson gson = new Gson();
            /* JSON to Java object, read it from a Json String. */
                streetLampMessage = gson.fromJson(json, StreetLampMessage.class);

            } catch (JsonParseException e) {
            /* wrong json format */
                e.printStackTrace();
                collector.ack(tuple);
                return;
            }

            emitValidLampTuple(tuple, streetLampMessage);
        }

    }

    /**
     * check and emit only valid tuples
     *
     * @param tuple             received from spout tuple
     * @param streetLampMessage parsed from tuple
     */
    private void emitValidLampTuple(Tuple tuple, StreetLampMessage streetLampMessage) {

        if (validStreetLampFormat(streetLampMessage)) {

            StreetLamp lamp = streetLampMessage.getStreetLamp();

            Integer id = lamp.getID();
            Address address = lamp.getAddress();
            Boolean on = lamp.isOn();
            String model = lamp.getLampModel().toString();
            Float consumption = lamp.getConsumption();
            Float intensity = lamp.getLightIntensity();
            Float naturalLightLevel = streetLampMessage.getNaturalLightLevel();
            LocalDateTime lifetime = lamp.getLifetime();
            Long timestamp = streetLampMessage.getTimestamp();

            Values values = new Values();
            values.add(id);
            values.add(address);
            values.add(on);
            values.add(model);
            values.add(consumption);
            values.add(intensity);
            values.add(lifetime);
            values.add(naturalLightLevel);
            values.add(timestamp);

            //System.out.println("[CINI] FILTERING : " + values.toString());

             /* anchor tuple to new streetLamp value */
            collector.emit(tuple, values);
            collector.ack(tuple);

        } else {

            collector.ack(tuple);
        }
    }

    /**
     * the street light should have a valid format
     * ex: intensity and naturalLight level should be percentages,
     * timestamp should not be too far in the past, etc
     *
     * @param streetLamp needed validation
     * @return true if valid, false otherwise
     */
    private boolean validStreetLampFormat(StreetLampMessage streetLamp) {
        // TODO check if 1st field is ID,2nd is an address, etc
        return true;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        outputFieldsDeclarer.declare(new Fields(Constant.ID, Constant.ADDRESS, Constant.ON,
                Constant.LAMP_MODEL, Constant.CONSUMPTION, Constant.INTENSITY, Constant.LIFETIME,
                Constant.NATURAL_LIGHT_LEVEL, Constant.TIMESTAMP));

    }


}