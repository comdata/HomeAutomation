package cm.homeautomation.mqtt.client;

import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.ebus.EBUSDataReceiver;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.fhem.FHEMDataReceiver;
import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;
import cm.homeautomation.jeromq.server.NoClassInformationContainedException;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import cm.homeautomation.services.hueinterface.HueInterface;
import io.quarkus.runtime.StartupEvent;

@AutoCreateInstance
public class MQTTReceiverClient implements MqttCallback {

	private static final String MQTT_EXCEPTION = "MQTT Exception.";
	private MqttClient client;
	private boolean run = true;
	private MemoryPersistence memoryPersistence = new MemoryPersistence();
	private static ObjectMapper mapper = new ObjectMapper();

	@Inject
	private HueInterface hueInterface;

	public MQTTReceiverClient() {
		runClient();
	}

	void startup(@Observes StartupEvent event) {
	}

	public void runClient() {

		try {
			connect();

			while (run) {
				keepClientConnected();

			}

		} catch (MqttException e) {
			LogManager.getLogger(this.getClass()).error(MQTT_EXCEPTION, e);
		}
	}

	private void keepClientConnected() {
		try {
			if (client != null) {
				if (!client.isConnected()) {
					LogManager.getLogger(this.getClass()).info("Not connected");
					tryToDisconnect();
					client.reconnect();
				}

			} else {
				LogManager.getLogger(this.getClass()).info("client is null");
				connect();
			}
			Thread.sleep(10000);

		} catch (MqttException | InterruptedException e) {
			LogManager.getLogger(this.getClass()).error("Exception while running client.", e);
		}
	}

	private void tryToDisconnect() {
		try {
			client.disconnectForcibly(100);
		} catch (MqttException e) {
			LogManager.getLogger(this.getClass()).error(MQTT_EXCEPTION, e);
		}
	}

	private void connect() throws MqttException {

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();

		String host = ConfigurationService.getConfigurationProperty("mqtt", "host");
		String port = ConfigurationService.getConfigurationProperty("mqtt", "port");

		client = new MqttClient("tcp://" + host + ":" + port, "HomeAutomation/" + randomUUIDString, memoryPersistence);
		client.setCallback(this);

		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setAutomaticReconnect(true);
		connOpt.setKeepAliveInterval(60);
		connOpt.setConnectionTimeout(60);
		connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

		client.connect(connOpt);

		client.subscribe("#");
		LogManager.getLogger(this.getClass()).info("Started MQTT client");
	}

	public void stopServer() {

		try {
			run = false;
			client.disconnect();
		} catch (MqttException e) {
			LogManager.getLogger(this.getClass()).error(MQTT_EXCEPTION, e);
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
		try {
			connect();
		} catch (MqttException e) {
			LogManager.getLogger(this.getClass()).error(MQTT_EXCEPTION, e);
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		byte[] payload = message.getPayload();
		String messageContent = new String(payload);
		LogManager.getLogger(this.getClass()).info("Got MQTT message: " + message.getId() + "/" + messageContent);

		EventBusService.getEventBus().post(new MQTTTopicEvent(topic, messageContent));

		try {
			Runnable receiver = null;
			if (topic.startsWith("/fhem")) {
				receiver = () -> FHEMDataReceiver.receiveFHEMData(topic, messageContent);
			} else if (topic.startsWith("ebusd/")) {
				receiver = () -> EBUSDataReceiver.receiveEBUSData(topic, messageContent);
			} else if (topic.startsWith("hueinterface")) {
				System.out.println(messageContent);
				HueEmulatorMessage hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
				hueInterface.handleMessage(hueMessage);
			} else {
				receiver = () -> {
					try {
						if (messageContent.startsWith("{")) {
							JSONSensorDataReceiver.receiveSensorData(messageContent);
						}
					} catch (NoClassInformationContainedException e) {
						LogManager.getLogger(this.getClass()).error("Got an exception while saving data.", e);
					}
				};
			}

			if (receiver != null) {
				new Thread(receiver).start();
			}

		} catch (Exception e) {
			LogManager.getLogger(this.getClass()).error("Got an exception while saving data.", e);
		}

		client.messageArrivedComplete(message.getId(), message.getQos());

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// nothing to do
	}

	public static void main(String[] args) {
		new MQTTReceiverClient().test();
	}

	private void test() {
		String messageContent = "{\n" + "  \"on\" : true,\n" + "  \"from\" : \"::ffff:192.168.1.88\",\n"
				+ "  \"on_off_command\" : true,\n" + "  \"payload\" : \"on\",\n" + "  \"change_direction\" : 0,\n"
				+ "  \"bri\" : 100,\n" + "  \"bri_normalized\" : 1,\n" + "  \"device_name\" : \"Stehlampe\",\n"
				+ "  \"light_id\" : \"5c33cb1ba93134\",\n" + "  \"port\" : 38301,\n"
				+ "  \"_msgid\" : \"4c9afd9b.6261b4\"\n" + "}";
		System.out.println(messageContent);
		HueEmulatorMessage hueMessage;
		try {
			hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
			hueInterface.handleMessage(hueMessage);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
