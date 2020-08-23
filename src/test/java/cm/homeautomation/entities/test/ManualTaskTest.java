package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.helper.GetterIsSetterCheck;
import cm.homeautomation.helper.MultiThreadExecutor;
import de.a9d3.testing.checks.PublicVariableCheck;

class ManualTaskTest {

	@Test
	public void baseTest() {
		MultiThreadExecutor executor = new MultiThreadExecutor();

		assertTrue(executor.execute(this.getClass(), Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
				// new EmptyCollectionCheck(),
				new GetterIsSetterCheck(),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}

}
