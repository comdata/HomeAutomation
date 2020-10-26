package cm.homeautomation.mqtt.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MQTTPubClient {

	@Inject
	static MQTTSender mqttSender;

	private MQTTPubClient() {
		// not to be created
	}
	
	public static void publish(String[] args) {

		String topic = args[0];
		String content = "";
		mqttSender.sendMQTTMessage(topic, content);

	}
}
