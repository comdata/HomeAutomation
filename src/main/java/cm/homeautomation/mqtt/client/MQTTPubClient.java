package cm.homeautomation.mqtt.client;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTPubClient {

	
    public static void publish(String[] args) {

        String topic        = args[0];
        String content      = "";
        int qos             = 2;
        String broker       = "tcp://localhost:1883";
        String clientId     = "HA ClientId";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            LogManager.getLogger(MQTTPubClient.class).debug("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            LogManager.getLogger(MQTTPubClient.class).debug("Connected");
            LogManager.getLogger(MQTTPubClient.class).debug("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            LogManager.getLogger(MQTTPubClient.class).debug("Message published");
            sampleClient.disconnect();
            LogManager.getLogger(MQTTPubClient.class).debug("Disconnected");
        } catch(MqttException me) {
        	LogManager.getLogger(MQTTPubClient.class).error("MQTT Exception "+me.getReasonCode(), me);
        }
    }
}
