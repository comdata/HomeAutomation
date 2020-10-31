package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.apache.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
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
						
						Runnable runThread = () -> {
							// Process the received message

							String topic = publish.getTopic().toString();
							String messageContent = new String(publish.getPayloadAsBytes());
							LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
							System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

							handleMessage(topic, messageContent);
						};
						new Thread(runThread).start();
					}).send().whenComplete((subAck, e) -> {
						if (e != null) {
							// Handle failure to subscribe
							LogManager.getLogger(this.getClass()).error(e);
						} else {
							// Handle successful subscription, e.g. logging or incrementing a metric
							LogManager.getLogger(this.getClass())
									.debug("successfully subscribed. Type: " + subAck.getType().name());
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

	private void handleMessage(String topic, String messageContent) {

		try {

			if (topic.startsWith("/fhem")) {
				EventBusService.getEventBus().post(new FHEMDataEvent(topic, messageContent));
			} else if (topic.startsWith("ebusd/")) {
				EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
				EventBusService.getEventBus().post(new EventObject(ebusMessageEvent));
			} else if (topic.startsWith("hueinterface")) {
				sendHueInterfaceMessage(messageContent);
			} else {
				if (messageContent.startsWith("{")) {
					EventBusService.getEventBus().post(new JSONDataEvent(messageContent));
				}
			}

			EventBusService.getEventBus().post(new MQTTTopicEvent(topic, messageContent));

		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void sendHueInterfaceMessage(String messageContent) {
		HueEmulatorMessage hueMessage;
		try {
			hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
			EventBusService.getEventBus().post(hueMessage);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}
}
