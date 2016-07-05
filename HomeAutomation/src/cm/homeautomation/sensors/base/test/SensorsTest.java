package cm.homeautomation.sensors.base.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cm.homeautomation.sensors.base.Sensors;

public class SensorsTest {

	@Test
	public void testGetInstance() {
		assertNotNull(Sensors.getInstance());
	}

}
