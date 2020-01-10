package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.ebus.EBUSDataReceiver;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.fhem.FHEMDataReceiver;
import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;
import cm.homeautomation.jeromq.server.NoClassInformationContainedException;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class MQTTReceiverClient implements MqttCallback {

	private static final String MQTT_EXCEPTION = "MQTT Exception.";
	private MqttClient client;
	private boolean run = true;
	private MemoryPersistence memoryPersistence = new MemoryPersistence();

	public MQTTReceiverClient() {
		runClient();
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
		
		String host=ConfigurationService.getConfigurationProperty("mqtt", "host");
		String port=ConfigurationService.getConfigurationProperty("mqtt", "port");

		client = new MqttClient("tcp://"+host+":"+port, "HomeAutomation/" + randomUUIDString, memoryPersistence);
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
		LogManager.getLogger(this.getClass()).info("Got MQTT message: " + messageContent);
		
		EventBusService.getEventBus().post(new MQTTTopicEvent(topic));
		
		try {
			Runnable receiver=null;
			if (topic.startsWith("/fhem")) {
				receiver = () -> FHEMDataReceiver.receiveFHEMData(topic, messageContent);
			} else if (topic.startsWith("ebusd/")) {
					receiver = () -> EBUSDataReceiver.receiveEBUSData(topic, messageContent);
			} else {
				receiver = () -> {
					try {
						JSONSensorDataReceiver.receiveSensorData(messageContent);
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
}
