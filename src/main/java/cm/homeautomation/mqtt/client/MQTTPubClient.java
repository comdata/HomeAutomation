package cm.homeautomation.mqtt.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

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

		System.out.println(topic + " " + content);
		MQTTSender.send(topic, content);

	}
}
