package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.log4j.LogManager;
import org.eclipse.microprofile.context.ManagedExecutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.fhem.FHEMDataEvent;
import cm.homeautomation.jeromq.server.JSONDataEvent;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.networkmonitor.NetworkScanResult;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
@Transactional(value = TxType.REQUIRES_NEW)
public class ReactiveMQTTReceiverClient {
	private static ObjectMapper mapper = new ObjectMapper();

	@Inject
	EventBus bus;
	
	@Inject
	ConfigurationService configurationService;
	
	@Inject
	ManagedExecutor executor;


	private void initClient() {

		String host = configurationService.getConfigurationProperty("mqtt", "host");
		int port = Integer.parseInt(configurationService.getConfigurationProperty("mqtt", "port"));

		Mqtt3AsyncClient client = buildAClient(host, port);

		Mqtt3AsyncClient zigbeeClient = buildAClient(host, port);
		Mqtt3AsyncClient ebusClient = buildAClient(host, port);
		Mqtt3AsyncClient hueClient = buildAClient(host, port);
		Mqtt3AsyncClient fhemClient = buildAClient(host, port);
		Mqtt3AsyncClient shellyClient = buildAClient(host, port);
		Mqtt3AsyncClient tasmotaClient = buildAClient(host, port);
		Mqtt3AsyncClient wledClient = buildAClient(host, port);
		Mqtt3AsyncClient networkServicesClient = buildAClient(host, port);
		Mqtt3AsyncClient espClient = buildAClient(host, port);
		
		wledClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				wledClient.subscribeWith().topicFilter("wled/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						mqttHandler(publish);
					};
					executor.runAsync(runThread);
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

		zigbeeClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				zigbeeClient.subscribeWith().topicFilter("zigbee2mqtt/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						mqttHandler(publish);
					};
					executor.runAsync(runThread);
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

		shellyClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				shellyClient.subscribeWith().topicFilter("shellies/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						mqttHandler(publish);
					};
					executor.runAsync(runThread);
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

		tasmotaClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				tasmotaClient.subscribeWith().topicFilter("tasmota/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						mqttHandler(publish);
					};
					executor.runAsync(runThread);
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
		
		espClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

				tasmotaClient.subscribeWith().topicFilter("esp/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						mqttHandler(publish);
					};
					executor.runAsync(runThread);
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
						// System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageEBUS(topic, messageContent);
					};
					executor.runAsync(runThread);
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
//						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageHUE(topic, messageContent);
					};
					executor.runAsync(runThread);
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

		networkServicesClient.connect().whenComplete((connAck, throwable) -> {
			if (throwable != null) {
				// Handle connection failure
			} else {

//					topicFilter("zigbee2mqtt").topicFilter("ebusd")

				hueClient.subscribeWith().topicFilter("networkServices/#").callback(publish -> {

					Runnable runThread = () -> {
						// Process the received message

						String topic = publish.getTopic().toString();
						String messageContent = new String(publish.getPayloadAsBytes());
						LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);

						if ("networkServices/scanResult".equals(topic)) {
							sendNetworkScanResult(messageContent);
						}
					};
					executor.runAsync(runThread);
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
//						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessageFHEM(topic, messageContent);
					};
					executor.runAsync(runThread);
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
//						System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

						handleMessage(topic, messageContent);
					};
					executor.runAsync(runThread);
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

	private void mqttHandler(Mqtt3Publish publish) {
		String topic = publish.getTopic().toString();
		String messageContent = new String(publish.getPayloadAsBytes());
		LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
		//System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

		handleMessageMQTT(topic, messageContent);
	}

	private Mqtt3AsyncClient buildAClient(String host, int port) {
		return MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString()).serverHost(host)
				.serverPort(port).automaticReconnect().applyAutomaticReconnect().buildAsync();
	}

	void startup(@Observes StartupEvent event) {
		initClient();

	}

	private void handleMessageMQTT(String topic, String messageContent) {

		try {

			MQTTTopicEvent mqttTopicEvent = new MQTTTopicEvent(topic, messageContent);
			bus.publish("MQTTTopicEvent", mqttTopicEvent);

		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessageFHEM(String topic, String messageContent) {
		try {
			FHEMDataEvent fhemDataEvent = new FHEMDataEvent(topic, messageContent);
			bus.publish("FHEMDataEvent", fhemDataEvent);

			handleMessageMQTT(topic, messageContent);

		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void handleMessageEBUS(String topic, String messageContent) {
		try {
			EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
			bus.publish("EBusMessageEvent", ebusMessageEvent);

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
				bus.publish("JSONDataEvent", jsonDataEvent);
				handleMessageMQTT(topic, messageContent);
			}
		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

	}

	private void sendHueInterfaceMessage(String messageContent) {
		HueEmulatorMessage hueMessage;
		try {
			System.out.println("hue: "+messageContent);
			hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);

			bus.publish("HueEmulatorMessage", hueMessage);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	private void sendNetworkScanResult(String messageContent) {
		NetworkScanResult networkScanResult;
		try {
			networkScanResult = mapper.readValue(messageContent, NetworkScanResult.class);

			bus.publish("NetworkScanResult", networkScanResult);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}
}
