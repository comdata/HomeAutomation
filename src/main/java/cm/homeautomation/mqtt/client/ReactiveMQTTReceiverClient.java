package cm.homeautomation.mqtt.client;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.ebus.EBUSDataReceiver;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.fhem.FHEMDataReceiver;
import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import cm.homeautomation.services.hueinterface.HueInterface;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;

@ApplicationScoped
public class ReactiveMQTTReceiverClient {
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Inject HueInterface hueInterface;
	
	@Incoming("homeautomation")
	public CompletionStage<Void> consume(MqttMessage<byte[]> message) {
		String topic=message.getTopic();
		String messageContent = new String(message.getPayload());

		try {
			Runnable receiver = null;
			if (topic.startsWith("/fhem")) {
				receiver = () -> FHEMDataReceiver.receiveFHEMData(topic, messageContent);
			} else if (topic.startsWith("ebusd/")) {
				receiver = () -> EBUSDataReceiver.receiveEBUSData(topic, messageContent);
			} else if (topic.startsWith("hueinterface")) {
				receiver = () -> {
					HueEmulatorMessage hueMessage;
					try {
						hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
						hueInterface.handleMessage(hueMessage);
					} catch (JsonMappingException e) {
					} catch (JsonProcessingException e) {

					}
				};
			} else {
				receiver = () -> {
					if (messageContent.startsWith("{")) {
						JSONSensorDataReceiver.receiveSensorData(messageContent);
					}
				};
			}

			if (receiver != null) {
				new Thread(receiver).start();
			}

			EventBusService.getEventBus().post(new MQTTTopicEvent(topic, messageContent));

		} catch (Exception e) {
			
		}
		
		return message.ack();
	}
}
