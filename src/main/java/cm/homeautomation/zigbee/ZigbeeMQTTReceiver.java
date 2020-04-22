package cm.homeautomation.zigbee;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.ZigBeeDevice;
import cm.homeautomation.entities.ZigbeeLight;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.remotecontrol.RemoteControlEventListener;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.zigbee.entities.ZigBeeTradfriRemoteControl;
import lombok.NonNull;

@AutoCreateInstance
public class ZigbeeMQTTReceiver {

	@NonNull
	private String zigbeeMqttTopic = ConfigurationService.getConfigurationProperty("zigbee", "mqttTopic");

	public ZigbeeMQTTReceiver() {
		EventBusService.getEventBus().register(this);

		updateDeviceList();
	}

	private void updateDeviceList() {
		MQTTSender.sendMQTTMessage(zigbeeMqttTopic + "/bridge/config/devices/get", "");
	}

	@Subscribe
	public void receiverMQTTTopicEvents(MQTTTopicEvent event) throws JsonMappingException, JsonProcessingException {
		@NonNull
		String topic = event.getTopic();

		if (topic.startsWith(zigbeeMqttTopic)) {

			// do zigbee magic
			String message = event.getMessage();
			LogManager.getLogger(this.getClass()).error("Got Zigbee message: " + message);

			if (topic.equals(zigbeeMqttTopic + "/bridge/config/devices")) {
				handleDeviceMessage(message);
			} else if (topic.startsWith(zigbeeMqttTopic + "/bridge")) {

			} else {
				String[] topicSplit = topic.split("/");

				if (topicSplit.length > 1) {
					String device = topicSplit[1];

					ZigBeeDevice zigbeeDevice = getZigbeeDevice(device);
					String modelID = zigbeeDevice.getModelID();

					ObjectMapper mapper = new ObjectMapper();
					JsonNode messageObject = mapper.readTree(message);

					if (zigbeeDevice.getManufacturerID().equals("4476")) {

						if (modelID.equals("TRADFRI remote control")) {
							handleTradfriRemoteControl(message, zigbeeDevice, messageObject);
						} else if (modelID.startsWith("TRADFRI bulb E14")) {
							System.out.println("E14. " + message);
							handleTradfriLight(message, zigbeeDevice, messageObject);
						} else if (modelID.startsWith("FLOALT panel")) {
							System.out.println("FLOALT. " + message);
							handleTradfriLight(message, zigbeeDevice, messageObject);
						} else if (modelID.startsWith("TRADFRI Driver")) {
							handleTradfriLight(message, zigbeeDevice, messageObject);
						}
					}

				}
			}
		}
	}

	private void handleTradfriLight(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		JsonNode actionNode = messageObject.get("action");

		// String action = actionNode.asText();

		System.out.println("Zigbee Action for tradfri light. ");

		EntityManager em = EntityManagerService.getManager();

		ZigbeeLight existingLight = em.find(ZigbeeLight.class, zigbeeDevice.getIeeeAddr());
		// create new light if not existing
		if (existingLight == null) {
			existingLight = new ZigbeeLight(zigbeeDevice.getIeeeAddr(), false);
			DimmableLight dimmableLight = new DimmableLight();

			dimmableLight.setName(zigbeeDevice.getFriendlyName());
			dimmableLight.setExternalId(zigbeeDevice.getIeeeAddr());
			dimmableLight.setType("ZIGBEE");
			dimmableLight.setMqttPowerOnTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			dimmableLight.setMqttPowerOffTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			dimmableLight.setMqttPowerOnMessage("{\"state\": \"on\"}");
			dimmableLight.setMqttPowerOffMessage("{\"state\": \"off\"}");

			em.getTransaction().begin();
			em.persist(dimmableLight);
			em.persist(existingLight);
			em.getTransaction().commit();
		}
	}

	private void handleTradfriRemoteControl(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		String action = messageObject.get("action").asText();

		EntityManager em = EntityManagerService.getManager();

		String ieeeAddr = zigbeeDevice.getIeeeAddr();
		ZigBeeTradfriRemoteControl existingRemote = em.find(ZigBeeTradfriRemoteControl.class, ieeeAddr);

		// create new remote if not existing
		if (existingRemote == null) {
			existingRemote = new ZigBeeTradfriRemoteControl(zigbeeDevice.getIeeeAddr(), false);

			em.getTransaction().begin();

			em.persist(existingRemote);
			em.getTransaction().commit();
		}

		if ("toggle".equals(action)) {
			existingRemote.setPowerOnState(existingRemote.isPowerOnState() ? false : true);
		}

		if ("arrow_right_click".equals(action)) {

		}

		if ("arrow_left_click".equals(action)) {

		}

		if ("brightness_up_click".equals(action)) {

		}

		if ("brightness_down_click".equals(action)) {

		}

		// TODO hold events
		RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
				existingRemote.isPowerOnState());

		EventBusService.getEventBus().post(remoteControlEvent);

		// update changed object in database
		em.getTransaction().begin();

		em.merge(existingRemote);
		em.getTransaction().commit();

		LogManager.getLogger(this.getClass()).error("remote control: " + message);
	}

	private ZigBeeDevice getZigbeeDevice(String device) {
		EntityManager em = EntityManagerService.getManager();
		List<ZigBeeDevice> resultList = em
				.createQuery("select zb from ZigBeeDevice zb where zb.friendlyName=:friendlyName", ZigBeeDevice.class)
				.setParameter("friendlyName", device).getResultList();

		ZigBeeDevice zigBeeDevice = resultList.get(0);

		return zigBeeDevice;
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
					ZigBeeDevice existingDevice = em.find(ZigBeeDevice.class, zigBeeDevice.getIeeeAddr());

					if (existingDevice == null) {
						em.persist(zigBeeDevice);
					} else {
						em.merge(zigBeeDevice);
					}

					em.getTransaction().commit();
				} catch (Exception e) {

				}
			}

		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("error parsing zigbee device message " + message, e);
		}
	}

	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();

		new RemoteControlEventListener();

		MQTTTopicEvent zigbeeDeviceEvent = new MQTTTopicEvent();
		zigbeeDeviceEvent.setTopic("zigbee2mqtt" + "/bridge/config/devices");
		zigbeeDeviceEvent.setMessage(
				"[{\"ieeeAddr\":\"0x00124b0018e27f02\",\"type\":\"Coordinator\",\"networkAddress\":0,\"friendly_name\":\"Coordinator\",\"softwareBuildID\":\"zStack12\",\"dateCode\":\"20190608\",\"lastSeen\":1587295440005},{\"ieeeAddr\":\"0x00158d000451907f\",\"type\":\"EndDevice\",\"networkAddress\":44744,\"model\":\"RTCGQ11LM\",\"vendor\":\"Xiaomi\",\"description\":\"Aqara human body movement and illuminance sensor\",\"friendly_name\":\"0x00158d000451907f\",\"manufacturerID\":4151,\"manufacturerName\":\"LUMI\",\"powerSource\":\"Battery\",\"modelID\":\"lumi.sensor_motion.aq2\",\"lastSeen\":1587294981305},{\"ieeeAddr\":\"0x000b57fffe521310\",\"type\":\"EndDevice\",\"networkAddress\":9892,\"model\":\"E1524/E1810\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI remote control\",\"friendly_name\":\"0x000b57fffe521310\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Battery\",\"modelID\":\"TRADFRI remote control\",\"hardwareVersion\":1,\"softwareBuildID\":\"2.3.014\",\"dateCode\":\"20190708\",\"lastSeen\":1587292304389},{\"ieeeAddr\":\"0xec1bbdfffe85e485\",\"type\":\"Router\",\"networkAddress\":673,\"model\":\"ICPSHC24-10EU-IL-1\",\"vendor\":\"IKEA\",\"description\":\"TRADFRI driver for wireless control (10 watt)\",\"friendly_name\":\"Arbeitsleuchte\",\"manufacturerID\":4476,\"manufacturerName\":\"IKEA of Sweden\",\"powerSource\":\"Mains (single phase)\",\"modelID\":\"TRADFRI Driver 10W\",\"hardwareVersion\":1,\"softwareBuildID\":\"1.2.245\",\"dateCode\":\"20170529\",\"lastSeen\":1587293860058}]");
		zigbeeMQTTReceiver.receiverMQTTTopicEvents(zigbeeDeviceEvent);

		MQTTTopicEvent zigbeeDeviceEvent2 = new MQTTTopicEvent();
		zigbeeDeviceEvent2.setTopic("zigbee2mqtt" + "/0x000b57fffe521310");
		zigbeeDeviceEvent2.setMessage("{\"linkquality\":86,\"battery\":60,\"action\":\"toggle\"}");
		zigbeeMQTTReceiver.receiverMQTTTopicEvents(zigbeeDeviceEvent2);

	}
}
