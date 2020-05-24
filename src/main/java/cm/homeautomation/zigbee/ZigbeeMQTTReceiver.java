package cm.homeautomation.zigbee;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.MQTTSwitch;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.entities.ZigBeeDevice;
import cm.homeautomation.entities.ZigbeeLight;
import cm.homeautomation.entities.ZigbeeMotionSensor;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.events.RemoteControlEvent.EventType;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.remotecontrol.RemoteControlEventListener;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.motion.MotionEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import cm.homeautomation.services.windowblind.WindowBlindService;
import cm.homeautomation.zigbee.entities.ZigBeeTradfriRemoteControl;
import lombok.NonNull;

public class ZigbeeMQTTReceiver {

	private static final String SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE = "select s from Sensor s where s.externalId=:externalId and s.sensorType=:sensorType";
	private static final int brightnessChangeIncrement = 10;
	private static final int BRIGHTNESSCHANGEINCREMENT2 = brightnessChangeIncrement;
	@NonNull
	private String zigbeeMqttTopic = ConfigurationService.getConfigurationProperty("zigbee", "mqttTopic");

	public ZigbeeMQTTReceiver() {
		EventBusService.getEventBus().register(this);

		updateDeviceList();
	}

	private void updateDeviceList() {
		MQTTSender.sendMQTTMessage(zigbeeMqttTopic + "/bridge/config/devices/get", "");
	}

	@Subscribe(threadMode = ThreadMode.ASYNC)
	public void receiveMQTTTopicEvents(MQTTTopicEvent event) throws JsonMappingException, JsonProcessingException {
		@NonNull
		String topic = event.getTopic();

		if (topic.startsWith(zigbeeMqttTopic)) {

			// do zigbee magic
			String message = event.getMessage();
			LogManager.getLogger(this.getClass()).debug("Got Zigbee message: " + message);

			if (topic.equals(zigbeeMqttTopic + "/bridge/config/devices")) {
				handleDeviceMessage(message);
			} else if (topic.startsWith(zigbeeMqttTopic + "/bridge")) {

			} else {
				String[] topicSplit = topic.split("/");

				if (topicSplit.length > 1) {
					String device = topicSplit[1];

					ZigBeeDevice zigbeeDevice = getZigbeeDevice(device);

					if (zigbeeDevice != null) {
						String modelID = zigbeeDevice.getModelID();

						ObjectMapper mapper = new ObjectMapper();
						JsonNode messageObject = mapper.readTree(message);

						recordBatteryLevel(zigbeeDevice, messageObject);
						recordLinkQuality(zigbeeDevice, messageObject);

						saveUpdateAvailableInformation(zigbeeDevice, messageObject);

						if (zigbeeDevice.getManufacturerID().equals("4476")) {

							if (modelID.equals("TRADFRI remote control")) {
								handleTradfriRemoteControl(message, zigbeeDevice, messageObject);
							} else if (modelID.equals("TRADFRI open/close remote")) {
								handleOpenCloseRemote(message, zigbeeDevice, messageObject);
							} else if (modelID.startsWith("TRADFRI bulb")) {

								if ("TRADFRI bulb E27 CWS opal 600lm".equals(modelID)) {
									handleTradfriLight(message, zigbeeDevice, messageObject, true);
								} else {
									handleTradfriLight(message, zigbeeDevice, messageObject, false);
								}
							} else if (modelID.startsWith("FLOALT panel")) {

								handleTradfriLight(message, zigbeeDevice, messageObject, false);
							} else if (modelID.startsWith("TRADFRI Driver")) {
								handleTradfriLight(message, zigbeeDevice, messageObject, false);
							} else if (modelID.startsWith("TRADFRI motion")) {
								handleMotionSensor(message, zigbeeDevice, messageObject);
							} else if (modelID.startsWith("TRADFRI control")) {
								handlePowerSocket(message, zigbeeDevice, messageObject);
							} else if (modelID.startsWith("FYRTUR block-out roller blind")) {
								handleWindowBlind(message, zigbeeDevice, messageObject);
							}

						}

						if (zigbeeDevice.getManufacturerID().equals("4416")) {
							if (modelID.equals("LWB006")) {
								handleTradfriLight(message, zigbeeDevice, messageObject, false);
							}
						}

						if (zigbeeDevice.getManufacturerID().equals("4151")) {
							if (modelID.equals("lumi.sensor_motion.aq2")) {
								handleMotionSensor(message, zigbeeDevice, messageObject);
							} else if (modelID.equals("lumi.sensor_wleak.aq1")) {
								handleWaterSensor(message, zigbeeDevice, messageObject);
							}
						}

						if (zigbeeDevice.getManufacturerID().equals("48042")) {
							if (modelID.equals("Plug 01")) {
								handlePowerSocket(message, zigbeeDevice, messageObject);
							}
						}
					} else {
						// we did not find the device so update the device list
						updateDeviceList();
					}
				}
			}
		}

	}

	private void handleOpenCloseRemote(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		String click = messageObject.get("click").asText();

		EntityManager em = EntityManagerService.getManager();

		String ieeeAddr = zigbeeDevice.getIeeeAddr();
		ZigBeeTradfriRemoteControl existingRemote = getOrCreateRemote(zigbeeDevice, em, ieeeAddr);

		RemoteControlEvent remoteControlEvent = new RemoteControlEvent();
		remoteControlEvent.setName(zigbeeDevice.getFriendlyName());
		remoteControlEvent.setTechnicalId(ieeeAddr);
		remoteControlEvent.setEventType(EventType.ON_OFF);
		remoteControlEvent.setClick(click);


		EventBusService.getEventBus().post(remoteControlEvent);

	}

	private void handleWindowBlind(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		EntityManager em = EntityManagerService.getManager();

		List<WindowBlind> resultList = em
				.createQuery("select w from WindowBlind w where w.externalId=:externalId", WindowBlind.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).getResultList();

		if (resultList != null && !resultList.isEmpty()) {

			JsonNode positionNode = messageObject.get("position");

			if (positionNode != null) {
				WindowBlind windowBlind = resultList.get(0);
				WindowBlindService windowBlindService = new WindowBlindService();
				windowBlindService.setPosition(windowBlind.getId(), positionNode.asText());
			}
		} else {
			WindowBlind windowBlind = new WindowBlind();

			windowBlind.setName(zigbeeDevice.getFriendlyName());
			windowBlind.setExternalId(zigbeeDevice.getIeeeAddr());
			windowBlind.setMqttDimTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");
			windowBlind.setMqttDimMessage("{\"position\": {DIMVALUE}}");

			em.getTransaction().begin();

			em.persist(windowBlind);
			em.getTransaction().commit();

			handleWindowBlind(message, zigbeeDevice, messageObject);
		}

	}

	private void saveUpdateAvailableInformation(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		JsonNode updateNode = messageObject.get("update_available");

		if (updateNode != null) {
			boolean updateAvailable = updateNode.asBoolean();

			EntityManager em = EntityManagerService.getManager();

			em.getTransaction().begin();

			zigbeeDevice.setUpdateAvailable(updateAvailable);
			em.merge(zigbeeDevice);

			em.getTransaction().commit();

		}
	}

	private void recordBatteryLevel(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		if ("Battery".equals(zigbeeDevice.getPowerSource())) {
			LogManager.getLogger(this.getClass()).debug("Device is battery powered");
			JsonNode batteryNode = messageObject.get("battery");

			if (batteryNode != null) {
				int batteryLevel = batteryNode.asInt();
				LogManager.getLogger(this.getClass()).debug("Device is battery powered - level: " + batteryLevel);

				recordBatteryLevelForDevice(zigbeeDevice, batteryLevel);
			}
		}
	}

	private void recordLinkQuality(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		JsonNode linkQualityNode = messageObject.get("linkquality");

		if (linkQualityNode != null) {
			int linkQuality = linkQualityNode.asInt();
			LogManager.getLogger(this.getClass()).debug("recording link quality " + linkQuality);

			recordLinkQualityForDevice(zigbeeDevice, linkQuality);
		}

	}

	private void handleWaterSensor(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		JsonNode leakNode = messageObject.get("leak");

		EntityManager em = EntityManagerService.getManager();

		if (leakNode != null) {

			boolean leakNodeBoolean = leakNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigbeeWaterSensor existingSensor = em.find(ZigbeeWaterSensor.class, ieeeAddr);

			if (existingSensor == null) {
				existingSensor = new ZigbeeWaterSensor(zigbeeDevice.getIeeeAddr(), leakNodeBoolean);

				em.getTransaction().begin();

				em.persist(existingSensor);
				em.getTransaction().commit();
			}

			existingSensor.setLeakDetected(leakNodeBoolean);

			em.getTransaction().begin();

			em.merge(existingSensor);
			em.getTransaction().commit();

			if (leakNodeBoolean) {
				WaterLeakEvent waterLeakEvent = WaterLeakEvent.builder().device(zigbeeDevice.getFriendlyName()).build();
				EventBusService.getEventBus().post(waterLeakEvent);
			}
		}

	}

	private void recordBatteryLevelForDevice(ZigBeeDevice zigbeeDevice, int batteryLevel) {
		EntityManager em = EntityManagerService.getManager();

		List<Sensor> sensorList = em
				.createQuery(SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE,
						Sensor.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).setParameter("sensorType", "battery")
				.getResultList();

		if (sensorList == null || sensorList.isEmpty()) {
			Sensor sensor = new Sensor();

			sensor.setExternalId(zigbeeDevice.getIeeeAddr());
			sensor.setSensorName(zigbeeDevice.getFriendlyName() + " Batterie");
			sensor.setSensorType("battery");
			sensor.setShowData(true);

			em.getTransaction().begin();
			em.persist(sensor);
			em.getTransaction().commit();

			recordBatteryLevelForDevice(zigbeeDevice, batteryLevel);
		} else {

			SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();

			saveRequest.setSensorId(sensorList.get(0).getId());
			SensorData sensorData = new SensorData();
			sensorData.setSensor(sensorList.get(0));
			sensorData.setValue(Integer.toString(batteryLevel));
			sensorData.setDateTime(new Date());
			saveRequest.setSensorData(sensorData);
			try {
				Sensors.getInstance().saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}
	}

	private void recordLinkQualityForDevice(ZigBeeDevice zigbeeDevice, int linkQuality) {
		EntityManager em = EntityManagerService.getManager();

		String linkQualityType = "linkquality";
		List<Sensor> sensorList = em
				.createQuery(SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE,
						Sensor.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).setParameter("sensorType", linkQualityType)
				.getResultList();

		if (sensorList == null || sensorList.isEmpty()) {
			Sensor sensor = new Sensor();

			sensor.setExternalId(zigbeeDevice.getIeeeAddr());
			sensor.setSensorName(zigbeeDevice.getFriendlyName() + " Link Quality");
			sensor.setSensorType(linkQualityType);
			sensor.setShowData(true);

			em.getTransaction().begin();
			em.persist(sensor);
			em.getTransaction().commit();

			recordLinkQualityForDevice(zigbeeDevice, linkQuality);
		} else {

			SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();

			saveRequest.setSensorId(sensorList.get(0).getId());
			SensorData sensorData = new SensorData();
			sensorData.setSensor(sensorList.get(0));
			sensorData.setValue(Integer.toString(linkQuality));
			sensorData.setDateTime(new Date());
			saveRequest.setSensorData(sensorData);
			try {
				Sensors.getInstance().saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}
	}

	private void recordIlluminanceLevelForDevice(ZigBeeDevice zigbeeDevice, int illuminance) {
		// TODO Auto-generated method stub

		EntityManager em = EntityManagerService.getManager();

		List<Sensor> sensorList = em
				.createQuery(SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE,
						Sensor.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).setParameter("sensorType", "illuminance")
				.getResultList();

		if (sensorList == null || sensorList.isEmpty()) {
			Sensor sensor = new Sensor();

			sensor.setExternalId(zigbeeDevice.getIeeeAddr());
			sensor.setSensorName(zigbeeDevice.getFriendlyName() + " illuminance");
			sensor.setSensorType("illuminance");
			sensor.setShowData(true);

			em.getTransaction().begin();
			em.persist(sensor);
			em.getTransaction().commit();

			recordIlluminanceLevelForDevice(zigbeeDevice, illuminance);
		} else {

			SensorDataSaveRequest saveRequest = new SensorDataSaveRequest();

			saveRequest.setSensorId(sensorList.get(0).getId());
			SensorData sensorData = new SensorData();
			sensorData.setSensor(sensorList.get(0));
			sensorData.setValue(Integer.toString(illuminance));
			sensorData.setDateTime(new Date());
			saveRequest.setSensorData(sensorData);
			try {
				Sensors.getInstance().saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}
	}

	private void handlePowerSocket(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		EntityManager em = EntityManagerService.getManager();

		List<MQTTSwitch> existingDeviceList = em
				.createQuery("select sw from MQTTSwitch sw where sw.externalId=:externalId", MQTTSwitch.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).getResultList();

		MQTTSwitch zigbeeSwitch = null;
		if (existingDeviceList == null || existingDeviceList.isEmpty()) {
			zigbeeSwitch = new MQTTSwitch();
			zigbeeSwitch.setExternalId(zigbeeDevice.getIeeeAddr());
			zigbeeSwitch.setMqttPowerOnTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			zigbeeSwitch.setMqttPowerOffTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			zigbeeSwitch.setMqttPowerOnMessage("{\"state\": \"ON\"}");
			zigbeeSwitch.setMqttPowerOffMessage("{\"state\": \"OFF\"}");
			zigbeeSwitch.setName(zigbeeDevice.getFriendlyName());
			zigbeeSwitch.setSwitchType("SOCKET");

			em.getTransaction().begin();
			em.persist(zigbeeSwitch);

			em.getTransaction().commit();
		} else {
			zigbeeSwitch = existingDeviceList.get(0);
		}

		JsonNode stateNode = messageObject.get("state");

		if (stateNode != null) {
			zigbeeSwitch.setLatestStatus(stateNode.asText());
			zigbeeSwitch.setLatestStatusFrom(new Date());
		}

		em.getTransaction().begin();
		em.persist(zigbeeSwitch);

		em.getTransaction().commit();
	}

	private void handleMotionSensor(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		// {"battery":100,"voltage":3015,"illuminance":558,"illuminance_lux":558,"linkquality":18,"occupancy":false}

		JsonNode occupancyNode = messageObject.get("occupancy");

		EntityManager em = EntityManagerService.getManager();

		if (occupancyNode != null) {

			boolean occupancyNodeBoolean = occupancyNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigbeeMotionSensor existingSensor = em.find(ZigbeeMotionSensor.class, ieeeAddr);

			if (existingSensor == null) {
				existingSensor = new ZigbeeMotionSensor(zigbeeDevice.getIeeeAddr(), occupancyNodeBoolean);

				em.getTransaction().begin();

				em.persist(existingSensor);
				em.getTransaction().commit();
			}

			existingSensor.setMotionDetected(occupancyNodeBoolean);
			em.getTransaction().begin();

			em.merge(existingSensor);
			em.getTransaction().commit();

			RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
					RemoteControlEvent.EventType.REMOTE);

			remoteControlEvent.setPoweredOnState(occupancyNodeBoolean);

			EventBusService.getEventBus().post(remoteControlEvent);

			MotionEvent motionDetectionEvent = new MotionEvent();
			motionDetectionEvent.setMac(ieeeAddr);
			motionDetectionEvent.setName(zigbeeDevice.getFriendlyName());
			motionDetectionEvent.setState(occupancyNodeBoolean);

			EventBusService.getEventBus().post(motionDetectionEvent);
		}

		JsonNode illuminanceNode = messageObject.get("illuminance");

		if (illuminanceNode != null) {
			int illuminanceLevel = illuminanceNode.asInt();
			LogManager.getLogger(this.getClass()).debug("illuminance level: " + illuminanceLevel);

			recordIlluminanceLevelForDevice(zigbeeDevice, illuminanceLevel);
		}
	}

	private void handleTradfriLight(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject, boolean color) {
		int brightness = 0;

		JsonNode brightnessNode = messageObject.get("brightness");

		if (brightnessNode != null) {
			brightness = brightnessNode.intValue();
		}

		EntityManager em = EntityManagerService.getManager();

		ZigbeeLight existingLight = em.find(ZigbeeLight.class, zigbeeDevice.getIeeeAddr());
		// create new light if not existing
		if (existingLight == null) {
			existingLight = new ZigbeeLight(zigbeeDevice.getIeeeAddr(), false, brightness);

			DimmableLight newLight = null;

			if (color) {
				RGBLight rgbLight = new RGBLight();
				newLight = rgbLight;

				rgbLight.setMqttColorTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");
				rgbLight.setMqttColorMessage(
						"{\"state\": \"ON\", \"brightness\": {DIMVALUE}, \"xy\": [{colorX}, {colorY}]}");
			} else {
				newLight = new DimmableLight();
			}

			newLight.setName(zigbeeDevice.getFriendlyName());
			newLight.setExternalId(zigbeeDevice.getIeeeAddr());
			newLight.setLightType("ZIGBEE");
			newLight.setMaximumValue(254);
			newLight.setMinimumValue(0);
			newLight.setMqttPowerOnTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			newLight.setMqttPowerOffTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

			newLight.setMqttPowerOnMessage("{\"state\": \"ON\", \"brightness\": {DIMVALUE}}");
			newLight.setMqttPowerOffMessage("{\"state\": \"OFF\"}");

			em.getTransaction().begin();
			em.persist(newLight);
			em.persist(existingLight);
			em.getTransaction().commit();
		}

		// TODO map generic light
		existingLight.setBrightness(brightness);

		JsonNode xyNode = messageObject.get("xy");

		if (xyNode != null && xyNode.isArray()) {
			List<JsonNode> xyNodeList = Arrays.asList(xyNode);

			existingLight.setX(Float.parseFloat(Double.toString(xyNodeList.get(0).asDouble())));
			existingLight.setY(Float.parseFloat(Double.toString(xyNodeList.get(1).asDouble())));
		}

		em.getTransaction().begin();

		em.merge(existingLight);
		em.getTransaction().commit();
	}

	private void handleTradfriRemoteControl(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		String action = messageObject.get("action").asText();

		EntityManager em = EntityManagerService.getManager();

		String ieeeAddr = zigbeeDevice.getIeeeAddr();
		ZigBeeTradfriRemoteControl existingRemote = getOrCreateRemote(zigbeeDevice, em, ieeeAddr);

		if ("toggle".equals(action)) {
			boolean sourcePowerState = existingRemote.isPowerOnState();
			boolean targetPowerState = sourcePowerState ? false : true;
			existingRemote.setPowerOnState(targetPowerState);
			existingRemote.setBrightness(targetPowerState ? 254 : 0);
			// TODO hold events
			RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
					RemoteControlEvent.EventType.REMOTE);

			remoteControlEvent.setPoweredOnState(existingRemote.isPowerOnState());

			EventBusService.getEventBus().post(remoteControlEvent);

			// update changed object in database
			em.getTransaction().begin();

			em.merge(existingRemote);
			em.getTransaction().commit();

			LogManager.getLogger(this.getClass()).debug("remote control: " + message);

		}

		if ("arrow_right_click".equals(action)) {

		}

		if ("arrow_left_click".equals(action)) {

		}

		if ("brightness_up_click".equals(action)) {
			RemoteControlBrightnessChangeEvent remoteControlBrightnessChangeEvent = new RemoteControlBrightnessChangeEvent();

			remoteControlBrightnessChangeEvent.setTechnicalId(existingRemote.getIeeeAddr());
			remoteControlBrightnessChangeEvent.setName(zigbeeDevice.getFriendlyName());
			remoteControlBrightnessChangeEvent.setPoweredOnState(existingRemote.isPowerOnState());
			int newBrightness = brightnessChange(existingRemote, BRIGHTNESSCHANGEINCREMENT2);

			remoteControlBrightnessChangeEvent.setBrightness(newBrightness);
			EventBusService.getEventBus().post(remoteControlBrightnessChangeEvent);

			// update changed object in database
			em.getTransaction().begin();
			existingRemote.setBrightness(newBrightness);
			existingRemote.setPowerOnState((newBrightness > 0));
			em.merge(existingRemote);
			em.getTransaction().commit();

		}

		if ("brightness_down_click".equals(action)) {
			if (existingRemote.isPowerOnState()) {
				RemoteControlBrightnessChangeEvent remoteControlBrightnessChangeEvent = new RemoteControlBrightnessChangeEvent();

				remoteControlBrightnessChangeEvent.setTechnicalId(existingRemote.getIeeeAddr());
				remoteControlBrightnessChangeEvent.setName(zigbeeDevice.getFriendlyName());
				remoteControlBrightnessChangeEvent.setPoweredOnState(existingRemote.isPowerOnState());

				int newBrightness = brightnessChange(existingRemote, -1 * BRIGHTNESSCHANGEINCREMENT2);

				remoteControlBrightnessChangeEvent.setBrightness(newBrightness);
				EventBusService.getEventBus().post(remoteControlBrightnessChangeEvent);

				// update changed object in database
				em.getTransaction().begin();
				existingRemote.setBrightness(newBrightness);
				existingRemote.setPowerOnState((newBrightness > 0));
				em.merge(existingRemote);
				em.getTransaction().commit();
			}
		}

	}

	private ZigBeeTradfriRemoteControl getOrCreateRemote(ZigBeeDevice zigbeeDevice, EntityManager em, String ieeeAddr) {
		ZigBeeTradfriRemoteControl existingRemote = em.find(ZigBeeTradfriRemoteControl.class, ieeeAddr);

		// create new remote if not existing
		if (existingRemote == null) {
			existingRemote = new ZigBeeTradfriRemoteControl(zigbeeDevice.getIeeeAddr(), false, 0);

			em.getTransaction().begin();

			em.persist(existingRemote);
			em.getTransaction().commit();
		}
		return existingRemote;
	}

	private int brightnessChange(ZigBeeTradfriRemoteControl existingRemote, int change) {
		int newBrightness = existingRemote.getBrightness() + change;

		if (newBrightness > 254) {
			newBrightness = 254;
		}

		if (newBrightness < 0) {
			newBrightness = 0;
		}

		return newBrightness;
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
		zigbeeMQTTReceiver.receiveMQTTTopicEvents(zigbeeDeviceEvent);

		MQTTTopicEvent zigbeeDeviceEvent2 = new MQTTTopicEvent();
		zigbeeDeviceEvent2.setTopic("zigbee2mqtt" + "/0x000b57fffe521310");
		zigbeeDeviceEvent2.setMessage("{\"linkquality\":86,\"battery\":60,\"action\":\"toggle\"}");
		zigbeeMQTTReceiver.receiveMQTTTopicEvents(zigbeeDeviceEvent2);

	}
}
