package cm.homeautomation.mqtt.client;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.fhem.FHEMDataEvent;
import cm.homeautomation.jeromq.server.JSONDataEvent;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;

@Singleton
public class ReactiveMQTTReceiverClient {
	private static ObjectMapper mapper = new ObjectMapper();

	private Mqtt3AsyncClient client = null;

	private void initClient() {
		if (client == null) {

			String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
			int port = Integer.parseInt(ConfigurationService.getConfigurationProperty("mqtt", "port"));

			client = MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString()).serverHost(host)
					.serverPort(port).automaticReconnect().applyAutomaticReconnect().buildAsync();

			client.connect().whenComplete((connAck, throwable) -> {
				if (throwable != null) {
					// Handle connection failure
				} else {
					client.subscribeWith().topicFilter("#").callback(publish -> {
						// Process the received message
						
						String topic=publish.getTopic().toString();
						String messageContent=new String(publish.getPayloadAsBytes());
						handleMessage(topic, messageContent);
					}).send().whenComplete((subAck, e) -> {
						if (e != null) {
							// Handle failure to subscribe
						} else {
							// Handle successful subscription, e.g. logging or incrementing a metric
						}
					});
				}
			});
		}

		if (!client.getState().isConnectedOrReconnect()) {
			client.connect();
		}

	}

	void startup(@Observes StartupEvent event) {
		initClient();

		EventBusService.getEventBus().register(this);
	}

	//@Incoming("homeautomation")
	public CompletionStage<Void> consume(MqttMessage<byte[]> message) {
		String topic = message.getTopic();
		String messageContent = new String(message.getPayload());

		handleMessage(topic, messageContent);

		return message.ack();
	}

	private void handleMessage(String topic, String messageContent) {
		System.out.println("Topic: " + topic + " " + messageContent);
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
