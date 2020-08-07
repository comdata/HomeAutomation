package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.RGBLight;
import de.a9d3.testing.checks.PublicVariableCheck;
import de.a9d3.testing.executer.SingleThreadExecutor;

class RGBLightTest {

	@Test
	public void baseTest() {
		SingleThreadExecutor executor = new SingleThreadExecutor();

		assertTrue(executor.execute(RGBLight.class, Arrays.asList(
				// new CopyConstructorCheck(),
				// new DefensiveCopyingCheck(),
				// new EmptyCollectionCheck(),
				// new GetterIsSetterCheck(),
				// new HashcodeAndEqualsCheck(),
				new PublicVariableCheck(true))));
	}

}
