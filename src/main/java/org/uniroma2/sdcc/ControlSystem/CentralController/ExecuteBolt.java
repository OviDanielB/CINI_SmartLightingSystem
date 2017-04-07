package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.google.gson.Gson;
import net.spy.memcached.MemcachedClient;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExecuteBolt extends BaseRichBolt{

    private OutputCollector collector;
    private Gson gson;

    private MemcachedClient memcachedClient;
    private static String MEMCACHED_SERVER = "localhost";
    private static int MEMCACHED_PORT = 11211;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = collector;
        this.gson = new Gson();

        try {
            memcachedClient = new MemcachedClient(new InetSocketAddress(MEMCACHED_SERVER, MEMCACHED_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        /* riceve la tupla contenente il valore di intensità della strada ottimale */
        /* pubblica su pub/sub il valore */

        int id =                    (int) tuple.getValueByField(AnomalyStreetLampMessage.ID);
        String address =            tuple.getValueByField(AnomalyStreetLampMessage.ADDRESS).toString();
        Lamp model =                (Lamp) tuple.getValueByField(AnomalyStreetLampMessage.LAMP_MODEL);
        Float consumption =         (Float) tuple.getValueByField(AnomalyStreetLampMessage.CONSUMPTION);
        LocalDateTime lifetime =    (LocalDateTime) tuple.getValueByField(AnomalyStreetLampMessage.LIFETIME);
        Timestamp timestamp =       (Timestamp) tuple.getValueByField(AnomalyStreetLampMessage.TIMESTAMP);
        Float adapted_intensity =   (Float) tuple.getValueByField(Constant.ADAPTED_INTENSITY);

        Address street = new Address();
        street.setName(address);

        StreetLamp adapted_lamp = new StreetLamp(
                id, true, model, street, consumption, adapted_intensity, lifetime);

        String json_adapted_lamp = gson.toJson(adapted_lamp);

        // TODO publish on pub/sub queue instead of memcached
        memcachedClient.set("adaptation"+address, 36000, json_adapted_lamp);

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
