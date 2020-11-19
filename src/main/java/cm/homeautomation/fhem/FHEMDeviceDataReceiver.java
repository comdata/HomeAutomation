package cm.homeautomation.fhem;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

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
@ApplicationScoped
public class FHEMDeviceDataReceiver {

	@Inject
	EntityManager em;

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
	public void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		Long referencedId = fhemDevice.getReferencedId();

		if (referencedId != null) {

			Device device = em.find(Device.class, fhemDevice.getReferencedId());

			String[] topicParts = topic.split("/");

			String topicLastPart = topicParts[topicParts.length - 1].toLowerCase();

			Sensor sensor = getSensorForTopic(device, topicLastPart);

			if (sensor != null) {
				try {
					Sensors.getInstance().saveSensorData(sensor.getId(), messageContent.split(" ")[0]);
				} catch (SensorDataLimitViolationException e) {
//					LogManager.getLogger(FHEMDeviceDataReceiver.class).error(
//							"Message for device: {} topic: {} message: {} received. Value exceeds limit.", device,
//							topicLastPart, messageContent);
				}
			} else {
//				LogManager.getLogger(FHEMDeviceDataReceiver.class).error(
//						"Message for device: {} topic: {} message: {} received. No Sensor attached.", device,
//						topicLastPart, messageContent);
			}
		} else {
//			LogManager.getLogger(FHEMDeviceDataReceiver.class)
//					.debug("referenced id is null for FHEM device: " + fhemDevice.getId());
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
		if (device != null) {
			Map<String, Sensor> sensors = device.getSensors();

			return sensors.get(topicLastPart);
		} else {
			return null;
		}
	}

}
