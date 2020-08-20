package cm.homeautomation.zigbee.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.helper.GetterIsSetterCheck;
import cm.homeautomation.zigbee.WaterLeakEvent;
import de.a9d3.testing.checks.CopyConstructorCheck;
import de.a9d3.testing.checks.EmptyCollectionCheck;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class WaterLeakEventTest {
    @Test
    void testBasicTest() {
        SingleThreadExecutor executor = new SingleThreadExecutor();
        assertTrue(executor.execute(WaterLeakEvent.class, Arrays.asList(
                // new CopyConstructorCheck(),
                // new DefensiveCopyingCheck(),
                // new EmptyCollectionCheck(),
                // new GetterIsSetterCheck(),
                // new HashcodeAndEqualsCheck(),
                new PublicVariableCheck(true))));
    }

    @Test
    void testTitle() {

        WaterLeakEvent event = WaterLeakEvent.builder().build();

        assertTrue("Water Leak".equals(event.getTitle()));
    }

    @Test
    void testMessage() {

        String device = "Test Water Leak Device";
        WaterLeakEvent event = WaterLeakEvent.builder().device(device).build();

        assertTrue(("Water Leak detected: " + device).equals(event.getMessageString()));
    }

    @Test
    void testDevice() {

        String device = "Test Water Leak Device";
        WaterLeakEvent event = WaterLeakEvent.builder().device(device).build();

        assertTrue(device.equals(event.getDevice()));
    }
}