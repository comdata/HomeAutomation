package cm.homeautomation.mqtt.client;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.services.window.WindowStateService;

public class FHEMWindowDataReceiver {

	private FHEMWindowDataReceiver() {
		// do not create instance
	}
	
	public static void receive(String topic, String messageContent, FHEMDevice fhemDevice) {

		String[] topicParts = topic.split("/");

		String topicLastPart = topicParts[topicParts.length-1].toLowerCase();

		switch (topicLastPart) {
		case "alarm":
			
			String state="";
			
			if (messageContent.contains("open")) {
				state="open";
			} else {
				state="closed";
			}
			
			((WindowStateService)WindowStateService.getInstance()).handleWindowState(fhemDevice.getReferencedId(), state);
			
			break;

		default:
			LogManager.getLogger(FHEMDataReceiver.class).error("Window: add handling for last part: " + topicLastPart);
		}
	}
}
