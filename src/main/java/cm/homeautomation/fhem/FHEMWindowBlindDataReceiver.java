package cm.homeautomation.fhem;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.windowblind.WindowBlindService;

public class FHEMWindowBlindDataReceiver {

	private FHEMWindowBlindDataReceiver() {
		// do not create an instance
	}

	public static void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length - 1].toLowerCase();

		if ("state".equalsIgnoreCase(topicLastPart)) {
			new WindowBlindService().setPosition(fhemDevice.getReferencedId(), messageContent.split(" ")[1]);
		}

	}

}
