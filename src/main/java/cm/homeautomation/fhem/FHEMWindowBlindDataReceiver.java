package cm.homeautomation.fhem;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.windowblind.WindowBlindService;

public class FHEMWindowBlindDataReceiver {

	private FHEMWindowBlindDataReceiver() {
		// do not create an instance
	}

	public static GenericStatus receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length - 1].toLowerCase();

		if ("state".equalsIgnoreCase(topicLastPart)) {
			return new WindowBlindService().setPosition(fhemDevice.getReferencedId(), messageContent.split(" ")[1]);
		}

		return null;
	}

}
