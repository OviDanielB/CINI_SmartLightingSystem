package org.uniroma2.sdcc.ControlSystem.CentralController;

import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uniroma2.sdcc.Constants;
import org.uniroma2.sdcc.Utils.HeliosLog;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Execute Bolt for main functionalities
 */
public class ExecuteBoltTest {

    private ExecuteBolt bolt;

    @Mock
    private TopologyContext topologyContext;

    @Mock
    private OutputCollector outputCollector;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        bolt = new ExecuteBolt();
        bolt.prepare(new Config(),topologyContext,outputCollector);

    }

    /**
     * test the main execute method;
     * functionalities tested elsewhere (SNSWriterTest)
     * @throws Exception
     */
    @Test
    public void execute() throws Exception {
        Tuple tuple = mock(Tuple.class);
        when(tuple.getValueByField(Constants.ID)).thenReturn(1234);
        when(tuple.getValueByField(Constants.ADAPTED_INTENSITY)).thenReturn(20f);
        bolt.execute(tuple);

    }

    /**
     * tests if incoming tuple is correctly
     * trasformed in JSON
     * @throws Exception
     */
    @Test
    public void composeMessage() throws Exception {
        Integer mockID = 5432;
        Float mockIntensity = 20f;

        Tuple tuple = mock(Tuple.class);
        when(tuple.getValueByField(Constants.ID)).thenReturn(mockID);
        when(tuple.getValueByField(Constants.ADAPTED_INTENSITY)).thenReturn(mockIntensity);
        String actual = bolt.composeMessage(tuple);

        /* composed message should be JSON */
        assertTrue(("{\"intensity\":" + Math.round(mockIntensity) + ",\"id\":" + mockID +"}").equals(actual));

    }

}