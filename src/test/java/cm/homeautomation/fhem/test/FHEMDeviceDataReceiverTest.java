package cm.homeautomation.fhem.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.fhem.FHEMDeviceDataReceiver;

public class FHEMDeviceDataReceiverTest {

	private Device device;

	@Before
	public void setup() {
		device = new Device();
		
		Map<String, Sensor> sensors=new HashMap<>();
		
		Sensor powerSensor = new Sensor();
		powerSensor.setSensorName("power");
		sensors.put("power", powerSensor);
		sensors.put("temperature", new Sensor());
		sensors.put("humidity", new Sensor());
		
		device.setSensors(sensors);
	}
	
	@Test
	public void testGetDevice() {
		
		
		
		Sensor sensorForTopic = FHEMDeviceDataReceiver.getSensorForTopic(device, "power");
		
		assertNotNull(sensorForTopic);
		assertTrue("power".equals(sensorForTopic.getSensorName()));
		
		
	}

}
