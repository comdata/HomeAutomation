package cm.homeautomation.fhem;

import javax.inject.Inject;
import javax.inject.Singleton;

import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTEventBusObject;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Singleton
public class MowerEventReceiver {

	@Inject
	EventBus bus;

	@ConsumeEvent(value = "MQTTEventBusObject", blocking = true)
	public void subscribe(MQTTEventBusObject eventObject) {

		String messageContent = eventObject.getMessageContent();
		String topic = eventObject.getTopic();

//		LogManager.getLogger(this.getClass()).info("MowerEventReceiver: Got MQTT message: {}", messageContent);

		// check if it is an error message
		if (topic.startsWith("/fhem/SILENO/mower-error")) {

			if (!"no_message".equals(messageContent)) {
//				LogManager.getLogger(this.getClass()).info("Mower: error: {}", messageContent);
				bus.publish("EventObject", new EventObject(new MowerErrorEvent(messageContent)));
			} else {
//				LogManager.getLogger(this.getClass()).info("Mower: not an error: {}", messageContent);
			}

		}
	}
}
