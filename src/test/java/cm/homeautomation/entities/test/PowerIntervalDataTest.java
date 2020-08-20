package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.PowerIntervalData;
import cm.homeautomation.helper.GetterIsSetterCheck;
import cm.homeautomation.helper.MultiThreadExecutor;
import de.a9d3.testing.checks.PublicVariableCheck;

class PowerIntervalDataTest {

	@Test
	public void baseTest() {
		MultiThreadExecutor executor = new MultiThreadExecutor();

		assertTrue(executor.execute(PowerIntervalData.class, Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
				// new EmptyCollectionCheck(),
				new GetterIsSetterCheck(),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}
}
