package cm.homeautomation.fhem.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.fhem.FHEMDataReceiver;

public class FHEMDataReceiverTest {
	
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
	public void testReceiveFHEMData() throws Exception {
		FHEMDataReceiver.receiveFHEMData("fhem/TestDataReceiver/testData", "testData");
	}

}
