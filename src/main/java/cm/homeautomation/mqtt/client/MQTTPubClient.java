package cm.homeautomation.mqtt.client;

public class MQTTPubClient {
	
	private MQTTPubClient() {
		// not to be constructed
	}

	public static void publish(String[] args) {

		String topic = args[0];
		String content = "";
		MQTTSender.sendMQTTMessage(topic, content);

	}
}
