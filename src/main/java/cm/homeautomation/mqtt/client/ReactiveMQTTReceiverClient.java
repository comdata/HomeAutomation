package cm.homeautomation.mqtt.client;

import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.ebus.EBUSDataReceiver;
import cm.homeautomation.ebus.EBusMessageEvent;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.fhem.FHEMDataReceiver;
import cm.homeautomation.jeromq.server.JSONSensorDataReceiver;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import cm.homeautomation.services.hueinterface.HueInterface;
import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;

@Startup
@ApplicationScoped
public class ReactiveMQTTReceiverClient {
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    HueInterface hueInterface;

    @Incoming("homeautomation")
    public CompletionStage<Void> consume(MqttMessage<byte[]> message) {
        String topic = message.getTopic();
        String messageContent = new String(message.getPayload());
        //System.out.println(message.getTopic() + ": " + new String(message.getPayload()));

        try {

            if (topic.startsWith("/fhem")) {
                FHEMDataReceiver.receiveFHEMData(topic, messageContent);
            } else if (topic.startsWith("ebusd/")) {
                EBusMessageEvent ebusMessageEvent = new EBusMessageEvent(topic, messageContent);
		        EventBusService.getEventBus().post(new EventObject(ebusMessageEvent));
            } else if (topic.startsWith("hueinterface")) {

                HueEmulatorMessage hueMessage;
                try {
                    hueMessage = mapper.readValue(messageContent, HueEmulatorMessage.class);
                    hueInterface.handleMessage(hueMessage);
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            } else {

                if (messageContent.startsWith("{")) {
                    JSONSensorDataReceiver.receiveSensorData(messageContent);
                }

            }

            EventBusService.getEventBus().post(new MQTTTopicEvent(topic, messageContent));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return message.ack();
    }
}
