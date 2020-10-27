package cm.homeautomation.mqtt.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.reactive.messaging.mqtt.MqttMessage;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@ApplicationScoped
public class MQTTSender {

    @Inject @Channel("homeautomationclient") Emitter<String> emitter;

	public void sendMQTTMessage(String topic, String messagePayload) {
		final Runnable mqttSendThread = () -> doSendSyncMQTTMessage(topic, messagePayload);
		new Thread(mqttSendThread).start();
	}

	public void sendSyncMQTTMessage(String topic, String messagePayload) {
		doSendSyncMQTTMessage(topic, messagePayload);
	}

	public void doSendSyncMQTTMessage(String topic, String messagePayload) {
        emitter.send(MqttMessage.of(topic, messagePayload));
	}
}