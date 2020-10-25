package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cm.homeautomation.configuration.ConfigurationService;
import io.quarkus.scheduler.Scheduled;
import lombok.NoArgsConstructor;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@NoArgsConstructor
public class MQTTSender {
	private MqttClient client;
	private static MQTTSender instance;
	private String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
	private String port = ConfigurationService.getConfigurationProperty("mqtt", "port");

	public MqttClient getClient() {
		return client;
	}

	public static MQTTSender getInstance() {

		if (instance == null) {
			instance = new MQTTSender();
		}
		return instance;
	}

	public static void sendMQTTMessage(String topic, String messagePayload) {
		final Runnable mqttSendThread = () -> getInstance().doSendSyncMQTTMessage(topic, messagePayload);
		new Thread(mqttSendThread).start();
	}

	public static void sendSyncMQTTMessage(String topic, String messagePayload) {
		getInstance().doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void doSendSyncMQTTMessage(String topic, String messagePayload) {

		try {
			if (client == null || !client.isConnected()) {
				initClient();
			}
			MqttMessage message = new MqttMessage();
			message.setPayload(messagePayload.getBytes());
			System.out.println(System.currentTimeMillis() + " sending message to: " + topic);
			client.publish(topic, message);
			System.out.println(System.currentTimeMillis() + " message sent: " + topic);
		} catch (MqttException e) {
		}
	}

	@Scheduled(every = "5s")
	public void initClient() throws MqttException {
		synchronized (this) {
			if (client == null || !client.isConnected()) {

				UUID uuid = UUID.randomUUID();
				String randomUUIDString = uuid.toString();

				client = new MqttClient("tcp://" + host + ":" + port, "HomeAutomation/" + randomUUIDString);

				MqttConnectOptions connOpt = new MqttConnectOptions();
				connOpt.setAutomaticReconnect(true);
				connOpt.setCleanSession(false);
				connOpt.setKeepAliveInterval(60);
				connOpt.setConnectionTimeout(30);
				connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

				client.connect(connOpt);
			}
		}
	}
}