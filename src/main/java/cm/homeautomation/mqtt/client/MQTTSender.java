package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


import cm.homeautomation.configuration.ConfigurationService;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;

import lombok.NoArgsConstructor;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@NoArgsConstructor
@ApplicationScoped
public class MQTTSender {
	private static MQTTSender instance;

    @Inject @Channel("homeautomation") Emitter<Object> emitter;

/*	public MqttClient getClient() {
		return client;
	}*/

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
        emitter.send(MqttMessage.of(topic, messagePayload));
	}
}