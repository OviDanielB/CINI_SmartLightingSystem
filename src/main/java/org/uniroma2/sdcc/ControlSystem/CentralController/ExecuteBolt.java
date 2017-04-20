package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.HeliosLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

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
    private static final String LOG_TAG = "[ExecuteBolt]";
    /* Amazon SNS connection */
    private final static String SNS_TOPIC_ARN = "arn:aws:sns:eu-west-1:369927171895:control";
    private final static String TOPIC = "control";


    /* consumer producer queue */
    private ArrayBlockingQueue<String> queue;
    /* if queue capacity maximum => producer blocks on put operation,
      similarly capacity 0 => consumer blocks on take */
    private static final Integer QUEUE_CAPACITY = 100;

    private ExecutorService executorService;
    private static final Integer THREAD_NUMBER = 4;
    


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
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        startSNSWritingThreadPool();

    }

    /**
     * create fixed thread pool of consumer threads waiting
     * on the queue for messages to send to SNS
     */
    private void startSNSWritingThreadPool() {

        executorService = Executors.newFixedThreadPool(THREAD_NUMBER);

        IntStream.range(0,THREAD_NUMBER).forEach(e -> {
            executorService.submit(new SNSWriter(queue));
        });


    }

    /**
     * ExecuteBolt operation on incoming tuple.
     *
     * @param tuple tuple received
     */
    @Override
    public void execute(Tuple tuple) {

        /* compose message to send by tuple values */
        String message = composeMessage(tuple);

        produceOnQueue(message);

        collector.ack(tuple);
    }

    /**
     * insert on queue message to be sent
     * block if capacity full
     * @param message to be sent
     */
    private void produceOnQueue(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            HeliosLog.logFail(LOG_TAG,"Can't insert into queue");
        }
    }

    /**
     * compose final message to send to each particular device
     * @param tuple collection of values from incoming stream
     * @return message composed; empty string if json parsing failed
     */
    private String composeMessage(Tuple tuple) {

        // retrieve data from incoming tuple
        Integer id =                (Integer) tuple.getValueByField(Constants.ID);
        Float adapted_intensity =   (Float) tuple.getValueByField(Constants.ADAPTED_INTENSITY);

        // composing control results
        HashMap<String,Integer> adapted_lamp = new HashMap<>(2);
        adapted_lamp.put("id", id);
        adapted_lamp.put("intensity", Math.round(adapted_intensity));

        String json_adapted_lamp;

        try {
            json_adapted_lamp = gson.toJson(adapted_lamp);
        } catch (JsonParseException e ){
            return "";
        }

        return json_adapted_lamp;
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


    /**
     * Runnable task which waits on a queue for messages
     * to be inserted and sends them on an sns connection
     */
    private class SNSWriter implements Runnable{

        private ArrayBlockingQueue<String> queue;

        private AmazonSNS sns;
        /* sns topic Amazon Resource Name to identify topic */
        private final static String SNS_TOPIC_ARN = "arn:aws:sns:eu-west-1:369927171895:control";

        public SNSWriter(ArrayBlockingQueue<String> queue) {
            this.queue = queue;
            snsConnect();
        }

        /**
         * connect to Amazon Web Services SNS service
         * in Ireland (EU_WEST_1)
         */
        private void snsConnect() {
            this.sns = AmazonSNSClient.builder().withRegion(Regions.EU_WEST_1).build();
        }

        @Override
        public void run() {
            try {
                String message = queue.take();
                sns.publish(new PublishRequest(SNS_TOPIC_ARN,message));
                System.out.println("Thread ID " + Thread.currentThread().getId() + " published on SNS");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
