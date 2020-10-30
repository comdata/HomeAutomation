package cm.homeautomation.mqtt.client;

import javax.inject.Singleton;

import org.apache.log4j.LogManager;

import cm.homeautomation.eventbus.EventBusService;
import io.quarkus.runtime.Startup;

@Startup
@Singleton
public class MQTTPubClient {

	private MQTTPubClient() {
		// not to be created
	}

	public static void publish(String[] args) {

		String topic = args[0];
		String content = "";

		LogManager.getLogger(MQTTPubClient.class).debug("Topic: " + topic + " " + content);
		
		EventBusService.getEventBus().post(new MQTTSendEvent(topic, content));

	}
}
