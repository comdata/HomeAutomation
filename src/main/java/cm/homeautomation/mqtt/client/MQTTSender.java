package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cm.homeautomation.configuration.ConfigurationService;
import lombok.extern.log4j.Log4j2;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@Log4j2
public class MQTTSender {
	private static MqttClient client;

	private MQTTSender() {
		// nothing to be done
	}

	public static void sendMQTTMessage(String topic, String messagePayload) {
		final Runnable mqttSendThread = () -> {
			try {

				initClient();

				log.debug("MQTT: sending message " + messagePayload + " to topic " + topic);

				MqttMessage message = new MqttMessage();
				message.setPayload(messagePayload.getBytes());
				client.publish(topic, message);
				log.debug("MQTT:  message sent " + messagePayload + " to topic " + topic);

			} catch (MqttException e) {
				log.error("Sending MQTT message: " + messagePayload + " failed.", e);
			}
		};
		new Thread(mqttSendThread).start();
	}

	private static void initClient() throws MqttException {
		if (client == null || !client.isConnected()) {

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
			String port = ConfigurationService.getConfigurationProperty("mqtt", "port");

			client = new MqttClient("tcp://" + host + ":" + port, "HomeAutomation/" + randomUUIDString);

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