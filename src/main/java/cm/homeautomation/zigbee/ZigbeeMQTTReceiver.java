package cm.homeautomation.zigbee;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.MQTTSwitch;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.entities.RemoteControl.RemoteType;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.entities.ZigBeeDevice;
import cm.homeautomation.entities.ZigbeeLight;
import cm.homeautomation.entities.ZigbeeMotionSensor;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.events.RemoteControlEvent.EventType;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.motion.MotionEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import cm.homeautomation.services.windowblind.WindowBlindPositionEvent;
import cm.homeautomation.zigbee.entities.ZigBeeTradfriRemoteControl;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import lombok.NonNull;

@Startup
@ApplicationScoped
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ZigbeeMQTTReceiver {
	@Inject
	MQTTSender mqttSender;

	@Inject
	EventBus bus;

	@Inject
	Sensors sensorsService;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	Sensors sensors;

	private static final String SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE = "select s from Sensor s where s.externalId=:externalId and s.sensorType=:sensorType";
	private static final int brightnessChangeIncrement = 10;

	@NonNull
	private String zigbeeMqttTopic;

	private Map<String, ZigBeeDevice> deviceMap = new HashMap<>();

	private void init() {
		initDeviceList();
		updateDeviceList();
	}

	void startup(@Observes StartupEvent event) {
		zigbeeMqttTopic = configurationService.getConfigurationProperty("zigbee", "mqttTopic");
		init();

	}

	private void updateDeviceList() {
		mqttSender.sendMQTTMessage(zigbeeMqttTopic + "/bridge/config/devices/get", "");
	}

	@ConsumeEvent(value = "MQTTTopicEvent", blocking = true)
	public void receiveMQTTTopicEvents(MQTTTopicEvent event) {
		@NonNull
		String topic = event.getTopic();

		// System.out.println("ZIGBEE: " + topic);
		if (topic.startsWith(zigbeeMqttTopic)) {

			try {

				// do zigbee magic
				String message = event.getMessage();

				// System.out.println("ZIGBEE2: " + topic + " " + message);

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
							System.out.println("Device not found: " + device);
							// we did not find the device so update the device list
							updateDeviceList();
						}
					}
				}
			} catch (JsonProcessingException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

	private void handleOpenCloseRemote(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		String click = messageObject.get("click").asText();

		String ieeeAddr = zigbeeDevice.getIeeeAddr();
		ZigBeeTradfriRemoteControl existingRemote = getOrCreateRemote(zigbeeDevice, em, ieeeAddr);

		RemoteControlEvent remoteControlEvent = new RemoteControlEvent();
		remoteControlEvent.setName(zigbeeDevice.getFriendlyName());
		remoteControlEvent.setTechnicalId(existingRemote.getIeeeAddr());
		remoteControlEvent.setEventType(EventType.ON_OFF);
		remoteControlEvent.setRemoteType(RemoteType.ZIGBEE);
		remoteControlEvent.setClick(click);

		bus.publish("RemoteControlEvent", remoteControlEvent);

	}

	private void handleWindowBlind(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		List<WindowBlind> resultList = em
				.createQuery("select w from WindowBlind w where w.externalId=:externalId", WindowBlind.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).getResultList();

		if (resultList != null && !resultList.isEmpty()) {

			JsonNode positionNode = messageObject.get("position");

			if (positionNode != null) {
				WindowBlind windowBlind = resultList.get(0);

				bus.publish("WindowBlindPosition",
						new WindowBlindPositionEvent(windowBlind.getId(), positionNode.asText()));
			}
		} else {

			WindowBlind windowBlind = new WindowBlind();

			windowBlind.setName(zigbeeDevice.getFriendlyName());
			windowBlind.setExternalId(zigbeeDevice.getIeeeAddr());
			windowBlind.setMqttDimTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");
			windowBlind.setMqttDimMessage("{\"position\": {DIMVALUE}}");
			em.persist(windowBlind);

			handleWindowBlind(message, zigbeeDevice, messageObject);
		}

	}

	private void saveUpdateAvailableInformation(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		JsonNode updateNode = messageObject.get("update_available");

		if (updateNode != null) {
			boolean updateAvailable = updateNode.asBoolean();

			zigbeeDevice.setUpdateAvailable(updateAvailable);
			em.merge(zigbeeDevice);

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

		if (leakNode != null) {

			boolean leakNodeBoolean = leakNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigbeeWaterSensor existingSensor = em.find(ZigbeeWaterSensor.class, ieeeAddr);

			if (existingSensor == null) {
				existingSensor = new ZigbeeWaterSensor(zigbeeDevice.getIeeeAddr(), leakNodeBoolean);

				em.persist(existingSensor);
			}

			existingSensor.setLeakDetected(leakNodeBoolean);

			em.merge(existingSensor);

			if (leakNodeBoolean) {
				WaterLeakEvent waterLeakEvent = WaterLeakEvent.builder().device(zigbeeDevice.getFriendlyName()).build();
				bus.publish("WaterLeakEvent", waterLeakEvent);
			}
		}

	}

	private void recordBatteryLevelForDevice(ZigBeeDevice zigbeeDevice, int batteryLevel) {

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

			em.persist(sensor);

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
				sensors.saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException | SecurityException | IllegalStateException e) {
				e.printStackTrace();
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

	private void recordLinkQualityForDevice(ZigBeeDevice zigbeeDevice, int linkQuality) {

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

			em.persist(sensor);

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
				sensorsService.saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException | SecurityException | IllegalStateException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

	private void recordIlluminanceLevelForDevice(ZigBeeDevice zigbeeDevice, int illuminance) {

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

			em.persist(sensor);

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
				sensors.saveSensorData(saveRequest);
			} catch (SensorDataLimitViolationException | SecurityException | IllegalStateException e) {
				e.printStackTrace();
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

	private void handlePowerSocket(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

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

			em.persist(zigbeeSwitch);

		} else {
			zigbeeSwitch = existingDeviceList.get(0);
		}

		JsonNode stateNode = messageObject.get("state");

		if (stateNode != null) {
			zigbeeSwitch.setLatestStatus(stateNode.asText());
			zigbeeSwitch.setLatestStatusFrom(new Date());
		}

		em.persist(zigbeeSwitch);

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void handleMotionSensor(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		// {"battery":100,"voltage":3015,"illuminance":558,"illuminance_lux":558,"linkquality":18,"occupancy":false}

		JsonNode occupancyNode = messageObject.get("occupancy");

		if (occupancyNode != null) {

			boolean occupancyNodeBoolean = occupancyNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigbeeMotionSensor existingSensor = em.find(ZigbeeMotionSensor.class, ieeeAddr);

			if (existingSensor == null) {
				existingSensor = new ZigbeeMotionSensor(zigbeeDevice.getIeeeAddr(), occupancyNodeBoolean);

				em.persist(existingSensor);

			}

			existingSensor.setMotionDetected(occupancyNodeBoolean);

			em.merge(existingSensor);

			RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
					RemoteControlEvent.EventType.REMOTE, RemoteType.ZIGBEE);

			remoteControlEvent.setPoweredOnState(occupancyNodeBoolean);

			bus.publish("RemoteControlEvent", remoteControlEvent);

			MotionEvent motionDetectionEvent = new MotionEvent();
			motionDetectionEvent.setMac(ieeeAddr);
			motionDetectionEvent.setName(zigbeeDevice.getFriendlyName());
			motionDetectionEvent.setState(occupancyNodeBoolean);
			motionDetectionEvent.setTimestamp(new Date());
			motionDetectionEvent.setType("ZIGBEE");

			// EventBusService.getEventBus().post(motionDetectionEvent);

			bus.publish("MotionEvent", motionDetectionEvent);
		}

		JsonNode illuminanceNode = messageObject.get("illuminance");

		if (illuminanceNode != null) {
			int illuminanceLevel = illuminanceNode.asInt();
			LogManager.getLogger(this.getClass()).debug("illuminance level: " + illuminanceLevel);

			recordIlluminanceLevelForDevice(zigbeeDevice, illuminanceLevel);
		}

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void handleTradfriLight(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject, boolean color) {

		int brightness = 0;

		JsonNode brightnessNode = messageObject.get("brightness");

		JsonNode stateNode = messageObject.get("state");

		if (brightnessNode != null) {
			brightness = brightnessNode.intValue();
		}

		String ieeeAddr = zigbeeDevice.getIeeeAddr();

		ZigbeeLight existingLight = em.find(ZigbeeLight.class, zigbeeDevice.getIeeeAddr());
		// create new light if not existing
		if (existingLight == null) {
			existingLight = new ZigbeeLight(zigbeeDevice.getIeeeAddr(), false, brightness);
			List<Light> lightList = em.createQuery("select l from Light l where l.externalId=:externalId", Light.class)
					.setParameter("externalId", ieeeAddr).getResultList();

			DimmableLight newLight = null;

			if (lightList == null || lightList.isEmpty()) {

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

				newLight.setExternalId(ieeeAddr);
				newLight.setLightType("ZIGBEE");
				newLight.setMaximumValue(254);
				newLight.setMinimumValue(0);
				newLight.setMqttPowerOnTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

				newLight.setMqttPowerOffTopic(zigbeeMqttTopic + "/" + zigbeeDevice.getFriendlyName() + "/set");

				newLight.setMqttPowerOnMessage("{\"state\": \"ON\", \"brightness\": {DIMVALUE}}");
				newLight.setMqttPowerOffMessage("{\"state\": \"OFF\"}");

			}

			if (newLight != null) {
				em.persist(newLight);
			}
			em.persist(existingLight);

		}

		// TODO map generic light
		existingLight.setBrightness(brightness);

		JsonNode xyNode = messageObject.get("xy");

		if (xyNode != null && xyNode.isArray()) {
			List<JsonNode> xyNodeList = Arrays.asList(xyNode);

			existingLight.setX(Float.parseFloat(Double.toString(xyNodeList.get(0).asDouble())));
			existingLight.setY(Float.parseFloat(Double.toString(xyNodeList.get(1).asDouble())));
		}

		if (stateNode != null) {
			existingLight.setPowerOnState("ON".equalsIgnoreCase(stateNode.asText()));
		}

		em.merge(existingLight);

	}

	private void handleTradfriRemoteControl(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		JsonNode actionNode = messageObject.get("action");
		if (actionNode != null) {
			String action = actionNode.asText();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigBeeTradfriRemoteControl existingRemote = getOrCreateRemote(zigbeeDevice, em, ieeeAddr);

			if ("toggle".equals(action)) {
				boolean sourcePowerState = existingRemote.isPowerOnState();
				boolean targetPowerState = sourcePowerState ? false : true;
				existingRemote.setPowerOnState(targetPowerState);
				existingRemote.setBrightness(targetPowerState ? 254 : 0);
				// TODO hold events
				RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
						RemoteControlEvent.EventType.REMOTE, RemoteType.ZIGBEE);

				remoteControlEvent.setPoweredOnState(existingRemote.isPowerOnState());

				bus.publish("RemoteControlEvent", remoteControlEvent);

				// update changed object in database

				em.merge(existingRemote);

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
				int newBrightness = brightnessChange(existingRemote, brightnessChangeIncrement);

				remoteControlBrightnessChangeEvent.setBrightness(newBrightness);
				bus.publish("RemoteControlBrightnessChangeEvent", remoteControlBrightnessChangeEvent);

				// update changed object in database

				existingRemote.setBrightness(newBrightness);
				existingRemote.setPowerOnState((newBrightness > 0));
				em.merge(existingRemote);

			}

			if ("brightness_down_click".equals(action)) {
				if (existingRemote.isPowerOnState()) {
					RemoteControlBrightnessChangeEvent remoteControlBrightnessChangeEvent = new RemoteControlBrightnessChangeEvent();

					remoteControlBrightnessChangeEvent.setTechnicalId(existingRemote.getIeeeAddr());
					remoteControlBrightnessChangeEvent.setName(zigbeeDevice.getFriendlyName());
					remoteControlBrightnessChangeEvent.setPoweredOnState(existingRemote.isPowerOnState());

					int newBrightness = brightnessChange(existingRemote, -1 * brightnessChangeIncrement);

					remoteControlBrightnessChangeEvent.setBrightness(newBrightness);
					bus.publish("RemoteControlBrightnessChangeEvent", remoteControlBrightnessChangeEvent);

					// update changed object in database

					existingRemote.setBrightness(newBrightness);
					existingRemote.setPowerOnState((newBrightness > 0));
					em.merge(existingRemote);

				}
			}
		}

	}

	private ZigBeeTradfriRemoteControl getOrCreateRemote(ZigBeeDevice zigbeeDevice, EntityManager em, String ieeeAddr) {
		ZigBeeTradfriRemoteControl existingRemote = em.find(ZigBeeTradfriRemoteControl.class, ieeeAddr);

		// create new remote if not existing
		if (existingRemote == null) {
			existingRemote = new ZigBeeTradfriRemoteControl(zigbeeDevice.getIeeeAddr(), false, 0);

			em.persist(existingRemote);

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
		return deviceMap.get(device);
	}

	@Scheduled(every = "120s")
	public void initDeviceList() {
		final

		List<ZigBeeDevice> devices = em.createQuery("select zb from ZigBeeDevice zb", ZigBeeDevice.class)
				.getResultList();

		Map<String, ZigBeeDevice> deviceMapTemp = new HashMap<>();

		for (ZigBeeDevice zigBeeDevice : devices) {

			deviceMapTemp.put(zigBeeDevice.getFriendlyName(), zigBeeDevice);
		}

		deviceMap = deviceMapTemp;
	}

	private void handleDeviceMessage(String message) {

		try {
			ObjectMapper mapper = new ObjectMapper();

			List<ZigBeeDevice> deviceList = mapper.readValue(message, new TypeReference<List<ZigBeeDevice>>() {
			});

			for (ZigBeeDevice zigBeeDevice : deviceList) {
				try {

					ZigBeeDevice existingDevice = em.find(ZigBeeDevice.class, zigBeeDevice.getIeeeAddr());

					if (existingDevice == null) {
						em.persist(zigBeeDevice);
					} else {
						em.merge(zigBeeDevice);
					}

				} catch (Exception e) {

				}
			}

		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error("error parsing zigbee device message " + message, e);
		}

	}
}
