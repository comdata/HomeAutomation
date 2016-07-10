package cm.homeautomation.mqtt.client;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;

public class MQTTReceiverClient extends Thread implements MqttCallback {

	private MqttClient client;
	private boolean run = true;

	public MQTTReceiverClient() {

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		start();
	}

	public void start() {
		try {
			connect();

			while (run) {
				try {
					if (client != null) {
						if (!client.isConnected()) {
							connect();
						}

					} else {
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
		client = new MqttClient("tcp://localhost:1883", "HomeAutomation");
		client.setCallback(this);
		MqttConnectOptions connOpt = new MqttConnectOptions();

		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		// connOpt.setUserName(M2MIO_USERNAME);
		// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

		client.connect(connOpt);

		client.subscribe("/sensordata", 0);
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
		// TODO Auto-generated method stub
		System.out.println("trying reconnect to MQTT broker");
		// connect();
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		byte[] payload = message.getPayload();
		String messageContent = new String(payload);
		System.out.println("Got MQTT message: " + messageContent);
		JSONSensorDataReceiver.receiveSensorData(messageContent);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}
