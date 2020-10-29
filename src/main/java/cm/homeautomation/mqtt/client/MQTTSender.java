package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import org.greenrobot.eventbus.Subscribe;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.services.base.AutoCreateInstance;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@Singleton
public class MQTTSender {

	private Mqtt5BlockingClient publishClient = null;
	
	public MQTTSender() {
		EventBusService.getEventBus().register(this);
	}

	private void initClient() {
		if (publishClient == null) {

			String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
			int port = Integer.parseInt(ConfigurationService.getConfigurationProperty("mqtt", "port"));

			publishClient = Mqtt5Client.builder().identifier(UUID.randomUUID().toString()).serverHost(host)
					.serverPort(port).automaticReconnect().applyAutomaticReconnect().buildBlocking();

			
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
		//System.out.println("MQTT " + topic + " " + messagePayload);
		EventBusService.getEventBus().post(new MQTTSendEvent(topic, messagePayload));
		

	}

	@Subscribe
	public void send(MQTTSendEvent mqttSendEvent) {
		String topic=mqttSendEvent.getTopic();
		String messagePayload=mqttSendEvent.getPayload();
		
		initClient();
		Mqtt5Publish publishMessage = Mqtt5Publish.builder().topic(topic).qos(MqttQos.AT_LEAST_ONCE)
				.payload(messagePayload.getBytes()).build();
		publishClient.publish(publishMessage);
		//System.out.println("sending: " + topic + " " + messagePayload);
	}
}