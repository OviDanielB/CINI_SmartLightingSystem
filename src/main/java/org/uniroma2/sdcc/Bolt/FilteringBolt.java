package org.uniroma2.sdcc.Bolt;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.codec.binary.StringUtils;
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

import java.time.LocalDateTime;
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
                if (streetLampMessage == null) {
                    collector.ack(tuple);
                    return;
                }
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
     * @param tuple received from spout tuple
     * @param streetLampMessage parsed from tuple
     */
    private void emitValidLampTuple(Tuple tuple, StreetLampMessage streetLampMessage) {

        StreetLamp lamp;
        if ( (lamp = streetLampMessage.getStreetLamp()) != null) {

            Integer id = lamp.getID();
            Address address = lamp.getAddress();
            Integer cellID = lamp.getCellID();
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
            values.add(cellID);
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
        }
        collector.ack(tuple);
    }

    /**
     * Declare name of the output tuple fields.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(StreetLampMessage.ID, StreetLampMessage.ADDRESS,
                StreetLampMessage.CELL, StreetLampMessage.ON, StreetLampMessage.LAMP_MODEL,
                StreetLampMessage.CONSUMPTION, StreetLampMessage.INTENSITY, StreetLampMessage.LIFETIME,
                StreetLampMessage.NATURAL_LIGHT_LEVEL, StreetLampMessage.TIMESTAMP));
    }
}