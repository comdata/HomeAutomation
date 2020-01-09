package cm.homeautomation.ebus;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EBUSDataReceiver {

	private EBUSDataReceiver() {
		// do nothing
	}

	public static void receiveEBUSData(String topic, String messageContent) {
		log.debug("EBUS. Topic: " + topic + " message: " + messageContent);

		EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
		EventBusService.getEventBus().post(new EventObject(ebusMessageEvent));

	}

}
