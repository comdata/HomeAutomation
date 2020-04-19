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

@AutoCreateInstance
public class ZigbeeMQTTReceiver {

	@NonNull
	private String zigbeeMqttTopic= ConfigurationService.getConfigurationProperty("zigbee", "mqttTopic");

	public ZigbeeMQTTReceiver() {
		EventBusService.getEventBus().register(this);

		updateDeviceList();
	}

	private void updateDeviceList() {
		MQTTSender.sendMQTTMessage(zigbeeMqttTopic + "/bridge/config/devices/get", "");
	}

	@Subscribe
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) {
		@NonNull
		String topic = event.getTopic();

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

	public static void main(String[] args) {
		ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();

		MQTTTopicEvent zigbeeDeviceEvent = new MQTTTopicEvent();
		zigbeeDeviceEvent.setTopic("zigbee2mqtt" + "/bridge/config/devices");
		zigbeeDeviceEvent.setMessage("[{\"ieeeAddr\":\"0x00124b0018e27f02\",\"type\":\"Coordinator\",\"networkAddress\":0,\"friendly_name\":\"Coordinator\",\"softwareBuildID\":\"zStack12\",\"dateCode\":\"20190608\",\"lastSeen\":1587295440005},{\"ieeeAddr\":\"0x00158d000451907f\",\"type\":\"EndDevice\",\"networkAddress\":44744,\"model\":\"RTCGQ11LM\",\"vendor\":\"Xiaomi\",\"description\":\"Aqara human body movement and illuminance sensor\",\"friendly_name\":\"0x00158d000451907f\",\"manufacturerID\":4151,\"manufacturerName\":\"LUMI\",\"powerSource\":\"Battery\",\"modelID\":\"lumi.sensor_motion.aq2\",\"lastSeen\":1587294981305},{\"ieeeAddr\":\"0x000b57fffe521310\",\"type\":\"EndDevice\",\"networkAddress\":9892,\"model\":\"E1524/E1810\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI remote control\",\"friendly_name\":\"0x000b57fffe521310\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Battery\",\"modelID\":\"TRADFRI remote control\",\"hardwareVersion\":1,\"softwareBuildID\":\"2.3.014\",\"dateCode\":\"20190708\",\"lastSeen\":1587292304389},{\"ieeeAddr\":\"0xec1bbdfffe85e485\",\"type\":\"Router\",\"networkAddress\":673,\"model\":\"ICPSHC24-10EU-IL-1\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI driver for wireless control (10 watt)\",\"friendly_name\":\"Arbeitsleuchte\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Mains (single phase)\",\"modelID\":\"TRADFRI Driver 10W\",\"hardwareVersion\":1,\"softwareBuildID\":\"1.2.245\",\"dateCode\":\"20170529\",\"lastSeen\":1587293860058}]");
		zigbeeMQTTReceiver.receiverMQTTTopicEvents(zigbeeDeviceEvent);
	}
}
