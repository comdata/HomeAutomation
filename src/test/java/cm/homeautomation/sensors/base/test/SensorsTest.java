package cm.homeautomation.sensors.base.test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

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
	
	@Test
	public void testSetSensors() {
		Sensors instance = Sensors.getInstance();

		instance.setSensors(new ArrayList<TechnicalSensor>());
		List<TechnicalSensor> sensors = instance.getSensors();
		assertNotNull(sensors);
		assertTrue(sensors.isEmpty());
	}

}
