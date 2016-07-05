package cm.homeautomation.mqtt.client;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;

public class MQTTReceiverClient implements MqttCallback {

	private MqttClient client;

	public MQTTReceiverClient() {
		start();
	}

	public void start() {
		try {
			client = new MqttClient("tcp://localhost:1883", "HomeAutomation");
			client.setCallback(this);
			MqttConnectOptions connOpt = new MqttConnectOptions();
			
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(30);
			//connOpt.setUserName(M2MIO_USERNAME);
			//connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
			
			client.connect(connOpt);

			client.subscribe("/sensordata", 0);
			System.out.println("Started MQTT client");

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {

		try {
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
		start();
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
