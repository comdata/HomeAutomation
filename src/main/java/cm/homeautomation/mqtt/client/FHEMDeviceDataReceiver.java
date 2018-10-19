package cm.homeautomation.mqtt.client;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManager;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;

public class FHEMDeviceDataReceiver {

	public static void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		EntityManager em = EntityManagerService.getManager();

		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length];

		switch (topicLastPart.toLowerCase()) {
		case "power":
			break;

		default:
			LogManager.getLogger(FHEMDataReceiver.class).error("Device: add handling for last part: " + topicLastPart);
		}

	}

}
