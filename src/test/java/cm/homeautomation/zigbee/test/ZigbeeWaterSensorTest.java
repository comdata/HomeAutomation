package cm.homeautomation.zigbee.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.helper.GetterIsSetterCheck;
import cm.homeautomation.zigbee.ZigbeeWaterSensor;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class ZigbeeWaterSensorTest {

    @Test
    void testGetterSetter() {
        SingleThreadExecutor executor = new SingleThreadExecutor();
        assertTrue(executor.execute(ZigbeeWaterSensor.class, Arrays.asList(
                // new CopyConstructorCheck(),
                // new DefensiveCopyingCheck(),
                // new EmptyCollectionCheck(),
                new GetterIsSetterCheck(),
                // new HashcodeAndEqualsCheck(),
                new PublicVariableCheck(true))));
    }
}