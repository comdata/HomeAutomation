package cm.homeautomation.ebus;

import javax.inject.Inject;
import javax.inject.Singleton;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Singleton
public class EBUSDataReceiver {

	@Inject
	EventBus bus;
	
	private EBUSDataReceiver() {
		// do nothing
	}

	public static void receiveEBUSData(String topic, String messageContent) {
		log.debug("EBUS. Topic: " + topic + " message: " + messageContent);

		EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
		//bus.send("EventObject", new EventObject(ebusMessageEvent));

	}

}
