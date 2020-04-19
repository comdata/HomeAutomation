package cm.homeautomation.zigbee;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.ZigBeeDevice;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.base.AutoCreateInstance;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AutoCreateInstance
public class ZigbeeMQTTReceiver {

	private String zigbeeMqttTopic;

	public ZigbeeMQTTReceiver() {
		EventBusService.getEventBus().register(this);

		zigbeeMqttTopic = ConfigurationService.getConfigurationProperty("zigbee", "mqttTopic");

		updateDeviceList();
	}

	private void updateDeviceList() {
		MQTTSender.sendMQTTMessage(zigbeeMqttTopic + "/bridge/config/devices/get", "");
	}

	@Subscribe
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {
		@NonNull
		String topic = event.getTopic();
		log.debug(topic);

		if (topic.startsWith(zigbeeMqttTopic)) {

			// do zigbee magic
			String message = event.getMessage();
			LogManager.getLogger(this.getClass()).error("Got Zigbee message: " + message);

			if (topic.equals(zigbeeMqttTopic + "/bridge/config/devices")) {
				handleDeviceMessage(message);

			}
		}
	}

	private void handleDeviceMessage(String message) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			List<ZigBeeDevice> deviceList = mapper.readValue(message, new TypeReference<List<ZigBeeDevice>>() {
			});

			EntityManager em = EntityManagerService.getManager();

			for (ZigBeeDevice zigBeeDevice : deviceList) {
				try {
					em.getTransaction().begin();
					em.persist(zigBeeDevice);
					em.getTransaction().commit();
				} catch (Exception e) {

				}
			}

		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("error parsing zigbee device message " + message, e);
		}
	}

}
