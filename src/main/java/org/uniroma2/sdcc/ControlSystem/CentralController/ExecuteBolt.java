package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constant;
import org.uniroma2.sdcc.Model.AnomalyStreetLampMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * This Bolt is the last component of the Control System's MAPE architecture.
 * Retrieve incoming tuple from Plan, publish output result on queue
 * where is subscribed lamp Local Controller that effectively adapt lamp intensity
 * to the planned value.
 */
public class ExecuteBolt extends BaseRichBolt{

    private OutputCollector collector;
    private Gson gson;
    private AmazonSNS sns;
    private static final String LOG_TAG = "[CINI] [ExecuteBolt] ";
    /* Amazon SNS connection */
    private final static String SNS_TOPIC_ARN = "arn:aws:sns:eu-west-1:369927171895:control";
    private final static String TOPIC = "control";


    /**
     * Bolt initialization
     *
     * @param map map
     * @param topologyContext context
     * @param outputCollector collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.gson = new Gson();

        initializeSNSPublisher();
    }

    /**
     * ExecuteBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        // retrieve data from incoming tuple
        Integer id =                (Integer) tuple.getValueByField(AnomalyStreetLampMessage.ID);
        Float adapted_intensity =   (Float) tuple.getValueByField(Constant.ADAPTED_INTENSITY);

        // composing control results
        HashMap<String,Integer> adapted_lamp = new HashMap<>(2);
        adapted_lamp.put("id", id);
        adapted_lamp.put("intensity", Math.round(adapted_intensity));

        String json_adapted_lamp = gson.toJson(adapted_lamp);

        //publish to SNS topic
        try {

            sns.publish(new PublishRequest(SNS_TOPIC_ARN, json_adapted_lamp));

            System.out.println(LOG_TAG + "Sent to SNS topic: " + json_adapted_lamp);

        } catch (Exception e) {
            e.printStackTrace();
        }

        collector.ack(tuple);
    }

    /**
     * Connect to SNS to publish data of adapted lamp values.
     */
    private void initializeSNSPublisher() {

        this.sns = AmazonSNSClient.builder().withRegion(Regions.EU_WEST_1).build();
        sns.createTopic(new CreateTopicRequest(TOPIC));
    }

    /**
     * Declare name of the output tuple fields.
     *
     * @param outputFieldsDeclarer output fields declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // no output fields to declare
    }
}
