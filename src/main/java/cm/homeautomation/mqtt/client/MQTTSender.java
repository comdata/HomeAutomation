package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
public class MQTTSender {
	private static MqttClient client;

	private MQTTSender() {
		// nothing to be done
	}

	public static void sendMQTTMessage(String topic, String jsonMessage) {
		try {

			initClient();

			MqttMessage message = new MqttMessage();
			message.setPayload(jsonMessage.getBytes());
			client.publish(topic, message);
		} catch (MqttException e) {
			LogManager.getLogger(MqttClient.class).error("Sending MQTT message: "+jsonMessage+" failed.", e);
		}
	}

	private static void initClient() throws MqttException {
		if (client == null || !client.isConnected()) {

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			client = new MqttClient("tcp://localhost:1883", "HomeAutomation/" + randomUUIDString);

			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setAutomaticReconnect(true);
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(60);
			connOpt.setConnectionTimeout(30);
			connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

			client.connect(connOpt);
		}
	}
}