package cm.homeautomation.sensors.base.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import cm.homeautomation.sensors.base.Sensors;
import cm.homeautomation.sensors.base.TechnicalSensor;

public class SensorsTest {

	@Test
	public void testGetInstance() {
		assertNotNull(Sensors.getInstance());
	}
	
	@Test
	public void testGetSensors() {
		Sensors instance = Sensors.getInstance();

		List<TechnicalSensor> sensors = instance.getSensors();
		assertNotNull(sensors);
		assertTrue(sensors.isEmpty());
	}

}
