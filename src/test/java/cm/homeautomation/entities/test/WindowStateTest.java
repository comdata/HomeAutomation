package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.WindowState;
import de.a9d3.testing.checks.GetterIsSetterCheck;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class WindowStateTest {

	@Test
	public void baseTest() {
		SingleThreadExecutor executor = new SingleThreadExecutor();

		assertTrue(executor.execute(WindowState.class, Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
				// new EmptyCollectionCheck(),
				new GetterIsSetterCheck(),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}
}
