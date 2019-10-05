package cm.homeautomation.fhem;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTEventBusObject;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class MowerEventReceiver {

	public MowerEventReceiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void subscribe(MQTTEventBusObject eventObject) {

		String messageContent = eventObject.getMessageContent();
		String topic = eventObject.getTopic();

		LogManager.getLogger(this.getClass()).info("Got MQTT message: {}", messageContent);

		// check if it is an error message
		if (topic.startsWith("/fhem/SILENO/mower-error")) {

			if (!"no_message".equals(messageContent)) {
				LogManager.getLogger(this.getClass()).info("Mower: error: {}", messageContent);
				EventBusService.getEventBus().post(new EventObject(new MowerErrorEvent(messageContent)));
			} else {
				LogManager.getLogger(this.getClass()).info("Mower: not an error: {}", messageContent);
			}

		}
	}
}
