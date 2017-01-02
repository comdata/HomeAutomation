package cm.homeautomation.mqtt.client;

import java.util.UUID;

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
							System.out.println("Not connected");
							connect();
						}

					} else {
						System.out.println("client is null");
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
		//connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(60);
		connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		// connOpt.setUserName(M2MIO_USERNAME);
		// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

		client.connect(connOpt);

		client.subscribe("/sensordata");
		client.subscribe("/sensorState");
		client.subscribe("/distanceSensor");
		client.subscribe("/switch");
		System.out.println("Started MQTT client");
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
			System.out.println("force close failed.");
		}
		System.out.println("trying reconnect to MQTT broker");
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
		System.out.println("Got MQTT message: " + messageContent);
		try {
			JSONSensorDataReceiver.receiveSensorData(messageContent);
		} catch (Exception e) {
			System.out.println("Got an exception while saving data.");
		}
		client.messageArrivedComplete(message.getId(), message.getQos());

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}
