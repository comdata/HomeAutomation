package cm.homeautomation.entities.test;


import de.a9d3.testing.checks.*;
import de.a9d3.testing.executer.SingleThreadExecutor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.ZigBeeDevice;

class ZigBeeDeviceTest {

	 @Test
	    public void baseTest() {
	        SingleThreadExecutor executor = new SingleThreadExecutor();

	        assertTrue(executor.execute(ZigBeeDevice.class, Arrays.asList( 
                    //new CopyConstructorCheck(), 
                    //new DefensiveCopyingCheck(),
                    //new EmptyCollectionCheck(), 
                    new GetterIsSetterCheck(),
                    //new HashcodeAndEqualsCheck(), 
                    new PublicVariableCheck())));
	    }
}
