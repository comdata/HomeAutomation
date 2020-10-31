package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.inject.Singleton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.eventbus.EventBusService;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@Singleton
public class MQTTSender {

	private Mqtt3AsyncClient publishClient = null;

	public MQTTSender() {
		EventBusService.getEventBus().register(this);
	}

	private void initClient() {
		if (publishClient == null) {

			String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
			int port = Integer.parseInt(ConfigurationService.getConfigurationProperty("mqtt", "port"));

			publishClient = MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString()).serverHost(host)
					.serverPort(port).automaticReconnect().applyAutomaticReconnect().buildAsync();

			publishClient.connect().whenComplete((connAck, throwable) -> {
				if (throwable != null) {
					// Handle connection failure
				} else {
					
				}
			});
		}

		if (!publishClient.getState().isConnectedOrReconnect()) {
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
		System.out.println("MQTT OUTBOUND" + topic + " " + messagePayload);
		EventBusService.getEventBus().post(new MQTTSendEvent(topic, messagePayload));

	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void send(MQTTSendEvent mqttSendEvent) {
		String topic = mqttSendEvent.getTopic();
		String messagePayload = mqttSendEvent.getPayload();

		initClient();
		Mqtt3Publish publishMessage = Mqtt3Publish.builder().topic(topic).qos(MqttQos.AT_LEAST_ONCE)
				.payload(messagePayload.getBytes()).build();
		publishClient.publish(publishMessage);
		// System.out.println("sending: " + topic + " " + messagePayload);
	}
}