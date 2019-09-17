package cm.homeautomation.fhem;

import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;

/**
 * FHEM integration
 * 
 * @author cmertins
 *
 */
public class FHEMDeviceDataReceiver {

	private FHEMDeviceDataReceiver() {
		// do not create an instance
	}

	/**
	 * forward sensor data to the Sensors service for storing
	 * 
	 * @param topic
	 * @param messageContent
	 * @param fhemDevice
	 */
	public static void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		EntityManager em = EntityManagerService.getManager();

		Device device = em.find(Device.class, fhemDevice.getReferencedId());

		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length - 1].toLowerCase();

		Sensor sensor = getSensorForTopic(device, topicLastPart);

		if (sensor != null) {
			try {
				Sensors.getInstance().saveSensorData(sensor.getId(), messageContent.split(" ")[0]);
			} catch (SensorDataLimitViolationException e) {
				LogManager.getLogger(FHEMDeviceDataReceiver.class).error("Message for device: {} topic: {} message: {} received. Value exceeds limit.", device, topicLastPart, messageContent);
			}
		} else {
			LogManager.getLogger(FHEMDeviceDataReceiver.class).error("Message for device: {} topic: {} message: {} received. No Sensor attached.",  device, topicLastPart, messageContent);
		}

	}

	/**
	 * find a sensor for a topic in the list
	 * 
	 * @param device
	 * @param topicLastPart
	 * @return
	 */
	public static Sensor getSensorForTopic(Device device, String topicLastPart) {
		Map<String, Sensor> sensors = device.getSensors();

		return sensors.get(topicLastPart);
	}

}
