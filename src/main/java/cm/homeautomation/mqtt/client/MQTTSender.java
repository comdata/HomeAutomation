package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import cm.homeautomation.configuration.ConfigurationService;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@ApplicationScoped
public class MQTTSender {

	private static Mqtt5BlockingClient publishClient = null;

	private static void initClient() {
		if (publishClient == null) {

			String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
			int port = Integer.parseInt(ConfigurationService.getConfigurationProperty("mqtt", "port"));

			publishClient = Mqtt5Client.builder().identifier(UUID.randomUUID().toString()).serverHost(host)
					.serverPort(port).buildBlocking();

			publishClient.connect();
		}
	}

	public void sendMQTTMessage(String topic, String messagePayload) {
		doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void sendSyncMQTTMessage(String topic, String messagePayload) {
		doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void doSendSyncMQTTMessage(String topic, String messagePayload) {
		System.out.println("MQTT " + topic + " " + messagePayload);
		send(topic, messagePayload);

	}

	public static void send(String topic, String messagePayload) {
		initClient();
		Mqtt5Publish publishMessage = Mqtt5Publish.builder().topic(topic).qos(MqttQos.AT_LEAST_ONCE)
				.payload(messagePayload.getBytes()).build();
		publishClient.publish(publishMessage);
		System.out.println("sending: " + topic + " " + messagePayload);
	}
}