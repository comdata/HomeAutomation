package cm.homeautomation.mqtt.client;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.fhem.FHEMDataEvent;
import cm.homeautomation.jeromq.server.JSONDataEvent;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import cm.homeautomation.services.hueinterface.HueInterface;
import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;

@Singleton
public class ReactiveMQTTReceiverClient {
	private static ObjectMapper mapper = new ObjectMapper();

	@Inject
	HueInterface hueInterface;

	@Incoming("homeautomation")
	public CompletionStage<Void> consume(MqttMessage<byte[]> message) {
		String topic = message.getTopic();
		String messageContent = new String(message.getPayload());
		handleMessage(topic, messageContent);

		return message.ack();
	}

	private void handleMessage(String topic, String messageContent) {
		Runnable runThread = () -> {
			try {

				if (topic.startsWith("/fhem")) {
					EventBusService.getEventBus().post(new FHEMDataEvent(topic, messageContent));
				} else if (topic.startsWith("ebusd/")) {
					EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
					EventBusService.getEventBus().post(new EventObject(ebusMessageEvent));
				} else if (topic.startsWith("hueinterface")) {

					HueEmulatorMessage hueMessage;
					try {
						hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
						EventBusService.getEventBus().post(hueMessage);
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}

				} else {
					if (messageContent.startsWith("{")) {
						EventBusService.getEventBus().post(new JSONDataEvent(messageContent));
					}
				}

				EventBusService.getEventBus().post(new MQTTTopicEvent(topic, messageContent));

			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(runThread).start();
	}
}
