package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.log4j.LogManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import cm.homeautomation.dashbutton.DashButtonEvent;
import cm.homeautomation.ebus.EBusMessageEvent;
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
public class ReactiveMQTTReceiverClient implements MqttCallback {
	@Inject
	EventBus bus;

	@ConfigProperty(name = "mqtt.host")
	String host;

	@ConfigProperty(name = "mqtt.port")
	int port;

	@Inject
	ManagedExecutor executor;

	private ObjectMapper mapper = new ObjectMapper();

	private MqttClient client;

	private MemoryPersistence memoryPersistence = new MemoryPersistence();

	void startup(@Observes StartupEvent event) {
		initClient();

	}

	private void initClient() {
		try {
			System.out.println("Connecting MQTT");
			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			client = new MqttClient("tcp://" + host + ":" + port, "HomeAutomation/" + randomUUIDString,
					memoryPersistence);

			client.setCallback(this);

			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setAutomaticReconnect(true);
			connOpt.setCleanSession(false);
			connOpt.setKeepAliveInterval(10);
			connOpt.setConnectionTimeout(5);
			connOpt.setMaxInflight(10);
			connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

			client.connect(connOpt);

			client.subscribe("#");
			System.out.println("Connected to MQTT");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void connectionLost(Throwable cause) {

		try {
			client.close();
			client.disconnect();
		} catch (MqttException e1) {
			LogManager.getLogger(this.getClass()).error("force close failed.", e1);
		}
		LogManager.getLogger(this.getClass()).info("trying reconnect to MQTT broker");
		initClient();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		String messageContent = new String(message.getPayload());

		Runnable runThread = () -> {
			// Process the received message

			LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
			System.out.println("Topic: " + topic + " " + messageContent);

			if (topic.startsWith("wled")) {
				System.out.println("WLED");
				handleMessageMQTT(topic, messageContent);
			}

			if (topic.startsWith("zigbee2mqtt")) {
				handleMessageMQTT(topic, messageContent);
			}

			if (topic.startsWith("shellies")) {
				handleMessageMQTT(topic, messageContent);
			}
			if (topic.startsWith("tasmota")) {
				handleMessageMQTT(topic, messageContent);
			}

			if (topic.startsWith("esp")) {
				handleMessageMQTT(topic, messageContent);
			}

			if (topic.startsWith("ebusd")) {
				handleMessageEBUS(topic, messageContent);
			}

			if (topic.startsWith("dhcpEvent")) {
				LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
				// System.out.println("MQTT INBOUND: " + topic + " " + messageContent);

				sendDashButtonMessage(messageContent);
			}

			if (topic.startsWith("hueinterface")) {
				LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
				// System.out.println("MQTT INBOUND: " + topic + " " + messageContent);
				System.out.println("hue");
				handleMessageHUE(topic, messageContent);
			}

			if (topic.startsWith("networkServices")) {
				LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
				// System.out.println("MQTT INBOUND: " + topic + " " + messageContent);
				if (topic.equals("networkServices/scanResult")) {
					sendNetworkScanResult(messageContent);
				}
			}

			if (topic.startsWith("/fhem")) {
				LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
				handleMessageFHEM(topic, messageContent);
			}

			if (topic.startsWith("/sensordata")) {
				LogManager.getLogger(this.getClass()).debug("Topic: " + topic + " " + messageContent);
				handleMessage(topic, messageContent);
			}

		};
		executor.runAsync(runThread);

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

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
			e.printStackTrace();
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
			System.out.println("hue: " + messageContent);
			hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);

			bus.publish("HueEmulatorMessage", hueMessage);
		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
	}

	private void sendDashButtonMessage(String messageContent) {

		try {
			System.out.println("dash: " + messageContent);
			DashButtonEvent dashButtonEvent = mapper.readValue(messageContent, DashButtonEvent.class);

			bus.publish("DashButtonEvent", dashButtonEvent);
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
