package cm.homeautomation.mqtt.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import lombok.NoArgsConstructor;

/**
 * generic MQTT sender
 * 
 * @author christoph
 *
 */
@Singleton
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