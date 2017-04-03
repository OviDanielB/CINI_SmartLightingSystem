package org.uniroma2.sdcc;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.uniroma2.sdcc.Utils.SlidingWindowAvg;
import org.uniroma2.sdcc.Utils.SlotBasedAvg;

import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class SlidingWindowsAvgTest {

    @Mock
    private SlotBasedAvg<Object> slotBasedAvg;

    @InjectMocks
    SlidingWindowAvg<Object> slidingWindow;


    //    @DataProvider
    private Object[][] data() {
        return new Object[][]{{7, 2 * 60, (System.currentTimeMillis() / 1000) - 30, 6}};
    }

    @Test
    public void updatedConsumptionAvgMockitoTest() throws Exception {

        Integer slot;
        Object[][] data = data();
        Integer windowsLengthInSlot = (Integer) data[0][0];
        Long timestamp = (Long) data[0][2];
        Integer expected = (Integer) data[0][3];

        slidingWindow.setWindowLengthInSlots(windowsLengthInSlot);

        ArgumentCaptor<Integer> slotCapture = ArgumentCaptor.forClass(Integer.class);

        slidingWindow.updatedConsumptionAvg(new Object(), 0.0f, timestamp);
        Mockito.verify(slotBasedAvg).updateConsumptionAvg(Matchers.anyObject(), slotCapture.capture(), Matchers.anyFloat());

        slot = slotCapture.getValue();
        assertEquals((int) slot, (int) expected);
    }

    @Test
    public void getAVgsTest() {

        SlidingWindowAvg<Object> slidingWindowAvg = new SlidingWindowAvg<>();

        Object obj = new Object();
        for (int i = 0; i < 100; i++)
            slidingWindowAvg.updatedConsumptionAvg(obj, 1f, System.currentTimeMillis() / 1000);


        Map<Object, Float> result = slidingWindowAvg.getAVgs();

        assertNotNull(result);
        assertTrue(result.containsKey(obj));
        assertTrue(result.get(obj) == 1f);
    }

}
