package cm.homeautomation.mqtt.client;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;

public class MQTTReceiverClient extends Thread implements MqttCallback {

	private MqttClient client;
	private boolean run = true;
	private MemoryPersistence memoryPersistence = new MemoryPersistence();

	public MQTTReceiverClient() {

	}

	@Override
	public void run() {
		super.run();

		try {
			connect();

			while (run) {
				try {
					if (client != null) {
						if (!client.isConnected()) {
							LogManager.getLogger(this.getClass()).info("Not connected");
							try {
								client.disconnectForcibly(100);
							} catch (MqttException e) {

							}
							client.reconnect();
						}

					} else {
						LogManager.getLogger(this.getClass()).info("client is null");
						connect();
					}
					Thread.sleep(10000);

				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connect() throws MqttException, MqttSecurityException {

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();

		client = new MqttClient("tcp://localhost:1883", "HomeAutomation/" + randomUUIDString, memoryPersistence);
		client.setCallback(this);

		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setAutomaticReconnect(true);
		// connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(60);
		connOpt.setConnectionTimeout(60);
		connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		// connOpt.setUserName(M2MIO_USERNAME);
		// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

		client.connect(connOpt);

		client.subscribe("/sensordata");
		client.subscribe("/sensorState");
		client.subscribe("/distanceSensor");
		client.subscribe("/switch");
		LogManager.getLogger(this.getClass()).info("Started MQTT client");
	}

	public void stopServer() {

		try {
			run = false;
			client.disconnect();
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
		try {
			connect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		byte[] payload = message.getPayload();
		String messageContent = new String(payload);
		LogManager.getLogger(this.getClass()).info("Got MQTT message: " + messageContent);
		try {
			Runnable receiver = new Runnable() {

				@Override
				public void run() {
					JSONSensorDataReceiver.receiveSensorData(messageContent);

				}
			};
			new Thread(receiver).start();

		} catch (Exception e) {
			e.printStackTrace();
			LogManager.getLogger(this.getClass()).error("Got an exception while saving data.", e);
		}
		client.messageArrivedComplete(message.getId(), message.getQos());

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}
