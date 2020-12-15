package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@ApplicationScoped
public class MQTTSender {

	private MqttClient publishClient = null;

	@ConfigProperty(name = "mqtt.host")
	String host;

	@ConfigProperty(name = "mqtt.port")
	int port;

	@Inject
	EventBus bus;

	public MQTTSender() {
	}

	private void initClient() {
		try {
			if (publishClient == null) {

				UUID uuid = UUID.randomUUID();
				String randomUUIDString = uuid.toString();

				publishClient = new MqttClient("tcp://" + host + ":" + port, "HomeAutomation/" + randomUUIDString);

			}

			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setAutomaticReconnect(true);
			connOpt.setCleanSession(false);
			connOpt.setKeepAliveInterval(60);
			connOpt.setConnectionTimeout(30);
			connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

			if (!publishClient.isConnected()) {
				publishClient.connect(connOpt);
			}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendMQTTMessage(String topic, String messagePayload) {
		doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void sendSyncMQTTMessage(String topic, String messagePayload) {
		doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void doSendSyncMQTTMessage(String topic, String messagePayload) {
		// System.out.println("MQTT OUTBOUND " + topic + " " + messagePayload);

		bus.publish("MQTTSendEvent", new MQTTSendEvent(topic, messagePayload));

	}

	public void doSendSyncMQTTMessage(String topic, Object obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			String payload = mapper.writeValueAsString(obj);

			bus.publish("MQTTSendEvent", new MQTTSendEvent(topic, payload));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

	}

	@ConsumeEvent(value = "MQTTSendEvent", blocking = true)
	public void send(MQTTSendEvent mqttSendEvent) {
		try {
			String topic = mqttSendEvent.getTopic();
			String messagePayload = mqttSendEvent.getPayload();

			initClient();
			MqttMessage message = new MqttMessage();
			message.setQos(1);
			message.setPayload(messagePayload.getBytes());

			publishClient.publish(topic, message);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}