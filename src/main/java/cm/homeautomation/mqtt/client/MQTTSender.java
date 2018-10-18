package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
public class MQTTSender {
	private static MqttClient client;

	public MQTTSender() {
	}

	public static void sendMQTTMessage(String topic, String jsonMessage) {
		try {

			initClient();

			MqttMessage message = new MqttMessage();
			message.setPayload(jsonMessage.getBytes());
			client.publish(topic, message);
			/*
			 * client.disconnect(); client.close();
			 */
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void initClient() throws MqttException, MqttSecurityException {
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
			// connOpt.setUserName(M2MIO_USERNAME);
			// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

			client.connect(connOpt);
		}
	}
}