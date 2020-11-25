package cm.homeautomation.mqtt.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.LogManager;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;

@ApplicationScoped
public class MQTTPubClient {

	@Inject
	EventBus bus;
	private static MQTTPubClient instance;

	private MQTTPubClient() {
		// not to be created
		instance = this;
	}

	void startup(@Observes StartupEvent event) {

	}

	public void publish(MQTTSendEvent event) {
		bus.publish("MQTTSendEvent", event);
	}

	public static void publish(String[] args) {

		String topic = args[0];
		String content = "";

		LogManager.getLogger(MQTTPubClient.class).debug("Topic: " + topic + " " + content);

		instance.publish(new MQTTSendEvent(topic, content));

	}
}
