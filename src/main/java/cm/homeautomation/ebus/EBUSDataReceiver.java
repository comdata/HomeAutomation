package cm.homeautomation.ebus;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class EBUSDataReceiver {
	
	private EBUSDataReceiver() {
		// do nothing
	}

	public static void receiveEBUSData(String topic, String messageContent) {
		LogManager.getLogger(EBUSDataReceiver.class).debug("EBUS. Topic: "+ topic+" message: "+messageContent );
		EBusMessageEvent ebusMessageEvent=new EBusMessageEvent(topic, messageContent);
		EventBusService.getEventBus().post(new EventObject(ebusMessageEvent));
	}

}
