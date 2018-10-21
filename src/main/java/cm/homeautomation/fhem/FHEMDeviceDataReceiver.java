package cm.homeautomation.fhem;

import java.util.Map;

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

		String topicLastPart = topicParts[topicParts.length - 1].toLowerCase();

		switch (topicLastPart) {
		case "power":
			Sensor powerSensor = findSensorForTopic(device, topicLastPart);
			if (powerSensor != null) {
				Sensors.getInstance().saveSensorData(powerSensor.getId(), messageContent.split(" ")[0]);
			}
			break;

		default:
			Map<String, Sensor> sensors = device.getSensors();

			Sensor sensor = sensors.get(topicLastPart);

			if (sensor != null) {
				Sensors.getInstance().saveSensorData(sensor.getId(), messageContent.split(" ")[0]);
			}
		}

	}

	private static Sensor findSensorForTopic(Device device, String topicLastPart) {

		Map<String, Sensor> sensors = device.getSensors();
		for (String sensorKey : sensors.keySet()) {
			Sensor sensor = sensors.get(sensorKey);
			if (sensor.getSensorName().equals(topicLastPart)) {
				return sensor;
			}

		}

		return null;
	}

}
