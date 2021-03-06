package org.uniroma2.sdcc.ControlSystem.CentralController;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.HeliosLog;
import org.uniroma2.sdcc.Utils.JSONConverter;

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
    private static final String LOG_TAG = "[ExecuteBolt]";

    /* consumer producer queue */
    private ArrayBlockingQueue<String> queue;
    /* if queue capacity maximum => producer blocks on put operation,
      similarly capacity 0 => consumer blocks on take */
    private static final Integer QUEUE_CAPACITY = 5000;

    private ExecutorService executorService;
    private static final Integer THREAD_NUMBER = 10;

    protected static volatile Integer count = 0;

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
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        startSNSWritingThreadPool();
    }

    /**
     * Create fixed thread pool of consumer threads waiting
     * on the queue for messages to send to SNS
     */
    protected void startSNSWritingThreadPool() {

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
     * Insert on queue message to be sent
     * block if capacity full.
     *
     * @param message to be sent
     */
    protected void produceOnQueue(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
            HeliosLog.logFail(LOG_TAG,"Can't insert into queue");
        }
    }

    /**
     * Compose final message to send to each particular device.
     *
     * @param tuple collection of values from incoming stream
     * @return message composed; empty string if json parsing failed
     */
    protected String composeMessage(Tuple tuple) {

        // retrieve data from incoming tuple
        Integer id =                (Integer) tuple.getValueByField(Constants.ID);
        Float adapted_intensity =   (Float) tuple.getValueByField(Constants.ADAPTED_INTENSITY);

        // composing control results
        HashMap<String,Integer> adapted_lamp = new HashMap<>(2);
        adapted_lamp.put("id", id);
        adapted_lamp.put("intensity", Math.round(adapted_intensity));

        String json_adapted_lamp;

        json_adapted_lamp = JSONConverter.fromAdaptedLamp(adapted_lamp);

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
    protected static class SNSWriter implements Runnable{

        private ArrayBlockingQueue<String> queue;

        private AmazonSNS sns;
        /* sns topic Amazon Resource Name to identify topic */
        private final static String SNS_TOPIC_ARN = "arn:aws:sns:eu-west-1:369927171895:control";

        private PublishResult lastResult;

        public SNSWriter(ArrayBlockingQueue<String> queue) {
            this.queue = queue;
            snsConnect();
        }

        /**
         * Connect to Amazon Web Services SNS service
         * in Ireland (EU_WEST_1)
         */
        protected void snsConnect() {
            ExecuteBolt.count++;
            this.sns = AmazonSNSClient.builder().withRegion(Regions.EU_WEST_1).build();
            if (isConnected()) {
                HeliosLog.logOK(LOG_TAG,"SNS CONNECTED " + count);
            }
        }

        @Override
        public void run() {
            try {
                String message = queue.take();
                if (isConnected()) {
                    lastResult = sns.publish(new PublishRequest(SNS_TOPIC_ARN,message));

                    HeliosLog.logOK(LOG_TAG,"Thread " + Thread.currentThread().getId() + " wrote on SNS " + message);
                } else {
                    HeliosLog.logFail(LOG_TAG, "SNS connection not available");
                }
            } catch (Exception e) {
                e.printStackTrace();
                HeliosLog.logFail(LOG_TAG, "Message not sent on SNS.");
            }
        }

        /**
         * Check if sns connection is available.
         *
         * @return true if connection present, false otherwise
         */
        public boolean isConnected(){
            return sns != null;
        }

        /**
         * Check if last message on queue
         * was sent correctly.
         *
         * @return outcome
         */
        public boolean lastMessageSent(){
            if (lastResult != null){
                return lastResult.getMessageId() != null;
            }
            return false;
        }
    }
}
