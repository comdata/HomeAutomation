package cm.homeautomation.fhem;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import cm.homeautomation.mqtt.client.MQTTEventBusObject;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
public class MowerEventReceiver {

	@Inject
	EventBus bus;

	@ConsumeEvent(value = "MQTTEventBusObject", blocking = true)
	public void subscribe(MQTTEventBusObject eventObject) {

		String messageContent = eventObject.getMessageContent();
		String topic = eventObject.getTopic();

//		//LogManager.getLogger(this.getClass()).info("MowerEventReceiver: Got MQTT message: {}", messageContent);

		// check if it is an error message
		if (topic.startsWith("/fhem/SILENO/mower-error")) {

			if (!"no_message".equals(messageContent)) {
//				//LogManager.getLogger(this.getClass()).info("Mower: error: {}", messageContent);
				bus.publish("MowerErrorEvent",new MowerErrorEvent(messageContent));
			} else {
//				//LogManager.getLogger(this.getClass()).info("Mower: not an error: {}", messageContent);
			}

		}
	}
}
