package cm.homeautomation.mqtt.client;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.sensors.Sensors;

public class FHEMDeviceDataReceiver {

	private FHEMDeviceDataReceiver() {
		// do not create an instance
	}
	
	public static void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		EntityManager em = EntityManagerService.getManager();

		Device device = em.find(Device.class, fhemDevice.getReferencedId());
		
		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length-1].toLowerCase();

		switch (topicLastPart) {
		case "power":
			Sensor powerSensor = findSensorForTopic(device, topicLastPart);
			if ( powerSensor!=null) {
				Sensors.getInstance().saveSensorData(powerSensor.getId(), messageContent.split(" ")[0]);
			}
			break;

		default:
			LogManager.getLogger(FHEMDataReceiver.class).error("Device: add handling for last part: " + topicLastPart);
		}

	}

	private static Sensor findSensorForTopic(Device device, String topicLastPart) {
		
		for (Sensor sensor : device.getSensors()) {
			if (sensor.getSensorName().equals(topicLastPart)) {
				return sensor;
			}
			
		}
		
		return null;
	}

}
