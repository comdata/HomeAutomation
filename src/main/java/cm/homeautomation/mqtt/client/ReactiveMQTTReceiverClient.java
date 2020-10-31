package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
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
import io.vertx.core.eventbus.EventBus;

@Singleton
public class ReactiveMQTTReceiverClient {
	private static ObjectMapper mapper = new ObjectMapper();

	@Inject
	EventBus bus;
	
	private void initClient() {

		String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
		int port = Integer.parseInt(ConfigurationService.getConfigurationProperty("mqtt", "port"));

		Mqtt3AsyncClient client = buildAClient(host, port);

		Mqtt3AsyncClient zigbeeClient = buildAClient(host, port);
		Mqtt3AsyncClient ebusClient = buildAClient(host, port);
		Mqtt3AsyncClient hueClient = buildAClient(host, port);
		Mqtt3AsyncClient fhemClient = buildAClient(host, port);

		zigbeeClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				zigbeeClient.subscribeWith().topicFilter("zigbee2mqtt/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						String topic = publish.getTopic().toString();
						String messageContent = new String(publish.getPayloadAsBytes());
						LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageMQTT(topic, messageContent);
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

		ebusClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

//					topicFilter("zigbee2mqtt").topicFilter("ebusd")
				ebusClient.subscribeWith().topicFilter("ebusd/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						String topic = publish.getTopic().toString();
						String messageContent = new String(publish.getPayloadAsBytes());
						LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageEBUS(topic, messageContent);
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

		hueClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

//					topicFilter("zigbee2mqtt").topicFilter("ebusd")

				hueClient.subscribeWith().topicFilter("hueinterface/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						String topic = publish.getTopic().toString();
						String messageContent = new String(publish.getPayloadAsBytes());
						LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageHUE(topic, messageContent);
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

		fhemClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				fhemClient.subscribeWith().topicFilter("/fhem/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						String topic = publish.getTopic().toString();
						String messageContent = new String(publish.getPayloadAsBytes());
						LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageFHEM(topic, messageContent);
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

		client.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

//					topicFilter("zigbee2mqtt").topicFilter("ebusd")

				client.subscribeWith().topicFilter("/sensordata/#").callback(publish -> {

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

	private Mqtt3AsyncClient buildAClient(String host, int port) {
		return MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString()).serverHost(host)
				.serverPort(port).automaticReconnect().applyAutomaticReconnect().buildAsync();
	}

	void startup(@Observes StartupEvent event) {
		initClient();

		EventBusService.getEventBus().register(this);
	}

	private void handleMessageMQTT(String topic, String messageContent) {

		try {

			MQTTTopicEvent mqttTopicEvent = new MQTTTopicEvent(topic, messageContent);
			EventBusService.getEventBus().post(mqttTopicEvent);

			bus.send(MQTTTopicEvent.class.getName(), mqttTopicEvent);

		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessageFHEM(String topic, String messageContent) {
		try {
			FHEMDataEvent fhemDataEvent = new FHEMDataEvent(topic, messageContent);
			EventBusService.getEventBus().post(fhemDataEvent);
			
			bus.send(FHEMDataEvent.class.getName(), fhemDataEvent);
			
			handleMessageMQTT(topic, messageContent);
			
			
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessageEBUS(String topic, String messageContent) {
		try {
			EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
			EventObject eventObject = new EventObject(ebusMessageEvent);
			EventBusService.getEventBus().post(eventObject);
			bus.send(EventObject.class.getName(), eventObject);
			
			handleMessageMQTT(topic, messageContent);
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessageHUE(String topic, String messageContent) {
		try {
			sendHueInterfaceMessage(messageContent);
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessage(String topic, String messageContent) {

		try {

			if (messageContent.startsWith("{")) {
				JSONDataEvent jsonDataEvent = new JSONDataEvent(messageContent);
				EventBusService.getEventBus().post(jsonDataEvent);
				bus.send(JSONDataEvent.class.getName(), jsonDataEvent);
				handleMessageMQTT(topic, messageContent);
			}
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void sendHueInterfaceMessage(String messageContent) {
		HueEmulatorMessage hueMessage;
		try {
			hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
			EventBusService.getEventBus().post(hueMessage);
			
			bus.send(HueEmulatorMessage.class.getName(), hueMessage);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}
}
