package cm.homeautomation.fhem;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTEventBusObject;
import io.quarkus.runtime.StartupEvent;

@Singleton
public class MowerEventReceiver {

	public MowerEventReceiver() {
		EventBusService.getEventBus().register(this);
	}

	void startup(@Observes StartupEvent event) {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void subscribe(MQTTEventBusObject eventObject) {

		String messageContent = eventObject.getMessageContent();
		String topic = eventObject.getTopic();

//		LogManager.getLogger(this.getClass()).info("MowerEventReceiver: Got MQTT message: {}", messageContent);

		// check if it is an error message
		if (topic.startsWith("/fhem/SILENO/mower-error")) {

			if (!"no_message".equals(messageContent)) {
//				LogManager.getLogger(this.getClass()).info("Mower: error: {}", messageContent);
				EventBusService.getEventBus().post(new EventObject(new MowerErrorEvent(messageContent)));
			} else {
//				LogManager.getLogger(this.getClass()).info("Mower: not an error: {}", messageContent);
			}

		}
	}
}
