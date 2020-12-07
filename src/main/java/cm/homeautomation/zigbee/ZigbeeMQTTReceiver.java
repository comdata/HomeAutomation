package cm.homeautomation.zigbee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import cm.homeautomation.entities.ZigbeeContactSensor;
import cm.homeautomation.entities.ZigbeeDeviceGroup;
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
@ActivateRequestContext
@Transactional(value = TxType.REQUIRES_NEW)
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

	private Map<String, ZigBeeDevice> deviceIeeeMap;

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
	@Transactional
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
				} else if (topic.equals(zigbeeMqttTopic + "/bridge/info")) {
					handleBridgeInfo(message);

				} else if (topic.startsWith(zigbeeMqttTopic + "/bridge")) {

				} else {
					String[] topicSplit = topic.split("/");

					if (topicSplit.length > 1) {
						String device = topicSplit[1];

						if (!device.equals("bridge")) {

							ZigBeeDevice zigbeeDevice = getZigbeeDevice(device);

							if (zigbeeDevice != null) {
								// System.out.println(zigbeeDevice.getModelID());
								String modelID = zigbeeDevice.getModelID();

								ObjectMapper mapper = new ObjectMapper();
								JsonNode messageObject = mapper.readTree(message);

								recordBatteryLevel(zigbeeDevice, messageObject);
								recordLinkQuality(zigbeeDevice, messageObject);

								saveUpdateAvailableInformation(zigbeeDevice, messageObject);

								System.out.println("post save update");
								String manufacturerID = zigbeeDevice.getManufacturerID();
								System.out.println("manufacturer: " + manufacturerID + " modelid: " + modelID);

								if (manufacturerID.equals("4476")) {

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

								if (manufacturerID.equals("4416")) {
									if (modelID.equals("LWB006")) {
										handleTradfriLight(message, zigbeeDevice, messageObject, false);
									}
								}

								if (manufacturerID.equals("4619")) {
									if (modelID.equals("TY0202")) {
										handleMotionSensor(message, zigbeeDevice, messageObject);
									}
								}
								if (manufacturerID.equals("4098")) {
									if (modelID.startsWith("TS011F")) {
										handlePowerSocket(message, zigbeeDevice, messageObject);
									}
								}

								if (manufacturerID.equals("4151")) {
									if (modelID.equals("lumi.sensor_motion.aq2")) {
										handleMotionSensor(message, zigbeeDevice, messageObject);
									} else if (modelID.equals("lumi.sensor_wleak.aq1")) {
										handleWaterSensor(message, zigbeeDevice, messageObject);
									} else if (modelID.equals("lumi.sensor_magnet.aq2")) {
										System.out.println("contact sensor");
										handleContactSensor(message, zigbeeDevice, messageObject);
									}

								}

								if (manufacturerID.equals("48042")) {
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
				}
			} catch (JsonProcessingException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}

		}

	}

	private void handleBridgeInfo(String message) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode messageObject = mapper.readTree(message);
			JsonNode findValues = messageObject.get("config").get("groups");
			Iterator<JsonNode> iterator = findValues.iterator();

			while (iterator.hasNext()) {
				JsonNode jsonNode = iterator.next();

				String name = jsonNode.get("friendly_name").asText();

				List<ZigbeeDeviceGroup> resultList = em
						.createQuery("select zg from ZigbeeDeviceGroup zg where zg.name=:name", ZigbeeDeviceGroup.class)
						.setParameter("name", name).getResultList();

				if (resultList == null || resultList.isEmpty()) {
					ZigbeeDeviceGroup zigbeeDeviceGroup = new ZigbeeDeviceGroup();

					String asText = jsonNode.toString();
					System.out.println(1 + " " + asText);

					Iterator<JsonNode> elements = jsonNode.get("devices").elements();

					zigbeeDeviceGroup.setName(name);

					List<ZigBeeDevice> deviceList = new ArrayList<>();

					while (elements.hasNext()) {
						JsonNode next = elements.next();

						String ieeeAddr = next.asText();

						ZigBeeDevice zigbeeDevice = getZigbeeDeviceByIeee(ieeeAddr);
						System.out.println(zigbeeDevice.getFriendlyName());
						deviceList.add(zigbeeDevice);
					}

					zigbeeDeviceGroup.setDevices(deviceList);

					em.persist(zigbeeDeviceGroup);
				}
			}

		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		System.out.println("update availabe start");
		JsonNode updateNode = messageObject.get("update_available");

		if (updateNode != null) {
			boolean updateAvailable = updateNode.asBoolean();

			zigbeeDevice.setUpdateAvailable(updateAvailable);
			em.merge(zigbeeDevice);

		}
		System.out.println("update availabe end");
	}

	private void recordBatteryLevel(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		if ("Battery".equals(zigbeeDevice.getPowerSource())) {
			System.out.println("battery level start");
			LogManager.getLogger(this.getClass()).debug("Device is battery powered");
			JsonNode batteryNode = messageObject.get("battery");

			if (batteryNode != null) {
				int batteryLevel = batteryNode.asInt();
				LogManager.getLogger(this.getClass()).debug("Device is battery powered - level: " + batteryLevel);

				recordBatteryLevelForDevice(zigbeeDevice, batteryLevel);
			}
			System.out.println("battery level end");
		}
	}

	private void recordLinkQuality(ZigBeeDevice zigbeeDevice, JsonNode messageObject) {
		System.out.println("link quality start");
		JsonNode linkQualityNode = messageObject.get("linkquality");

		if (linkQualityNode != null) {
			int linkQuality = linkQualityNode.asInt();
			LogManager.getLogger(this.getClass()).debug("recording link quality " + linkQuality);

			recordLinkQualityForDevice(zigbeeDevice, linkQuality);
		}
		System.out.println("link quality end");
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

	private void handleContactSensor(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		System.out.println("contact sensor start");

		JsonNode contactNode = messageObject.get("contact");

		if (contactNode != null) {

			boolean contactNodeBoolean = contactNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			ZigbeeContactSensor existingSensor = em.find(ZigbeeContactSensor.class, ieeeAddr);

			if (existingSensor == null) {
				existingSensor = new ZigbeeContactSensor(zigbeeDevice.getIeeeAddr(), contactNodeBoolean);

				em.persist(existingSensor);
			}

			existingSensor.setContact(contactNodeBoolean);

			em.merge(existingSensor);

			WindowContactEvent windowContactEvent = WindowContactEvent.builder().device(zigbeeDevice.getFriendlyName())
					.id(zigbeeDevice.getIeeeAddr()).contact(contactNodeBoolean).build();
			bus.publish("WindowContactEvent", windowContactEvent);
		}
		System.out.println("contact sensor end");
	}

	private void recordBatteryLevelForDevice(ZigBeeDevice zigbeeDevice, int batteryLevel) {

		List<Sensor> sensorList = em
				.createQuery(SELECT_S_FROM_SENSOR_S_WHERE_S_EXTERNAL_ID_EXTERNAL_ID_AND_S_SENSOR_TYPE_SENSOR_TYPE,
						Sensor.class)
				.setParameter("externalId", zigbeeDevice.getIeeeAddr()).setParameter("sensorType", "battery")
				.getResultList();

		if (sensorList == null || sensorList.isEmpty()) {
			System.out.println("battery level sensor empty");
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

			System.out.println("battery level - before save");
			// sensors.saveSensorData(saveRequest);
			bus.publish("SensorDataSaveRequest", saveRequest);
			System.out.println("battery level - after save");

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
			bus.publish("SensorDataSaveRequest", saveRequest);
			

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

	private void handleMotionSensor(String message, ZigBeeDevice zigbeeDevice, JsonNode messageObject) {

		// {"battery":100,"voltage":3015,"illuminance":558,"illuminance_lux":558,"linkquality":18,"occupancy":false}
		System.out.println("Motion: start");
		JsonNode occupancyNode = messageObject.get("occupancy");

		if (occupancyNode != null) {

			boolean occupancyNodeBoolean = occupancyNode.asBoolean();

			String ieeeAddr = zigbeeDevice.getIeeeAddr();
			System.out.println("Motion: find sensor");
			try {
				List<ZigbeeMotionSensor> existingSensors = em
						.createQuery("select z from ZigbeeMotionSensor z where z.ieeeAddr=:ieeeAddr",
								ZigbeeMotionSensor.class)
						.setParameter("ieeeAddr", ieeeAddr).getResultList();

				ZigbeeMotionSensor existingSensor = null;

				if (existingSensors == null || existingSensors.isEmpty()) {
					System.out.println("Motion: sensor empty");
					existingSensor = new ZigbeeMotionSensor(zigbeeDevice.getIeeeAddr(), occupancyNodeBoolean);

					em.persist(existingSensor);

				} else {
					existingSensor = existingSensors.get(0);
				}

				System.out.println("Motion: motion detected");
				existingSensor.setMotionDetected(occupancyNodeBoolean);

				em.merge(existingSensor);
				System.out.println("Motion: motion event merged");

				RemoteControlEvent remoteControlEvent = new RemoteControlEvent(zigbeeDevice.getFriendlyName(), ieeeAddr,
						RemoteControlEvent.EventType.REMOTE, RemoteType.ZIGBEE);

				remoteControlEvent.setPoweredOnState(occupancyNodeBoolean);

				bus.publish("RemoteControlEvent", remoteControlEvent);
				System.out.println("Motion: remote control event sent");

				MotionEvent motionDetectionEvent = new MotionEvent();
				motionDetectionEvent.setMac(ieeeAddr);
				motionDetectionEvent.setName(zigbeeDevice.getFriendlyName());
				motionDetectionEvent.setState(occupancyNodeBoolean);
				motionDetectionEvent.setTimestamp(new Date());
				motionDetectionEvent.setType("ZIGBEE");
				motionDetectionEvent.setRoom(zigbeeDevice.getRoom());

				// EventBusService.getEventBus().post(motionDetectionEvent);

				bus.publish("MotionEvent", motionDetectionEvent);
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	private ZigBeeDevice getZigbeeDeviceByIeee(String ieeeAddress) {
		return deviceIeeeMap.get(ieeeAddress);
	}

	@Scheduled(every = "120s")
	public void initDeviceList() {
		final

		List<ZigBeeDevice> devices = em.createQuery("select zb from ZigBeeDevice zb", ZigBeeDevice.class)
				.getResultList();

		Map<String, ZigBeeDevice> deviceMapTemp = new HashMap<>();
		Map<String, ZigBeeDevice> deviceIeeeMapTemp = new HashMap<>();

		for (ZigBeeDevice zigBeeDevice : devices) {

			deviceMapTemp.put(zigBeeDevice.getFriendlyName(), zigBeeDevice);
			deviceIeeeMapTemp.put(zigBeeDevice.getIeeeAddr(), zigBeeDevice);
		}

		deviceMap = deviceMapTemp;
		deviceIeeeMap = deviceIeeeMapTemp;
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

	public static void main(String[] args) {
		ZigbeeMQTTReceiver zigbeeMQTTReceiver = new ZigbeeMQTTReceiver();
		String message = "{\r\n" + "	\"commit\": \"6b32f30\",\r\n" + "	\"config\": {\r\n"
				+ "		\"advanced\": {\r\n" + "			\"adapter_concurrent\": null,\r\n"
				+ "			\"availability_blacklist\": [],\r\n" + "			\"availability_blocklist\": [],\r\n"
				+ "			\"availability_passlist\": [],\r\n" + "			\"availability_timeout\": 0,\r\n"
				+ "			\"availability_whitelist\": [],\r\n" + "			\"cache_state\": true,\r\n"
				+ "			\"cache_state_persistent\": true,\r\n"
				+ "			\"cache_state_send_on_startup\": true,\r\n" + "			\"channel\": 11,\r\n"
				+ "			\"elapsed\": false,\r\n" + "			\"ext_pan_id\": [\r\n" + "				221,\r\n"
				+ "				221,\r\n" + "				221,\r\n" + "				221,\r\n"
				+ "				221,\r\n" + "				221,\r\n" + "				221,\r\n"
				+ "				221\r\n" + "			],\r\n"
				+ "			\"homeassistant_discovery_topic\": \"homeassistant\",\r\n"
				+ "			\"homeassistant_legacy_triggers\": true,\r\n"
				+ "			\"homeassistant_status_topic\": \"hass/status\",\r\n"
				+ "			\"last_seen\": \"ISO_8601\",\r\n" + "			\"legacy_api\": true,\r\n"
				+ "			\"log_directory\": \"/app/data/log/%TIMESTAMP%\",\r\n"
				+ "			\"log_file\": \"log.txt\",\r\n" + "			\"log_level\": \"info\",\r\n"
				+ "			\"log_output\": [\r\n" + "				\"console\",\r\n" + "				\"file\"\r\n"
				+ "			],\r\n" + "			\"log_rotation\": true,\r\n" + "			\"log_syslog\": {\r\n"
				+ "				\r\n" + "			},\r\n" + "			\"pan_id\": 6754,\r\n"
				+ "			\"report\": false,\r\n" + "			\"soft_reset_timeout\": 0,\r\n"
				+ "			\"timestamp_format\": \"YYYY-MM-DD HH:mm:ss\"\r\n" + "		},\r\n"
				+ "		\"ban\": [],\r\n" + "		\"blocklist\": [],\r\n" + "		\"device_options\": {\r\n"
				+ "			\r\n" + "		},\r\n" + "		\"devices\": {\r\n"
				+ "			\"0x000b57fffe521310\": {\r\n"
				+ "				\"friendly_name\": \"Fernbedienung Arbeitszimmer\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe8a45d1\": {\r\n"
				+ "				\"friendly_name\": \"Stehlampe Lennard 3\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe8b8be8\": {\r\n" + "				\"friendly_name\": \"SD2\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffe8cd8b9\": {\r\n"
				+ "				\"friendly_name\": \"Stehlampe Lennard 2\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe8cf5ac\": {\r\n" + "				\"friendly_name\": \"Ballon Lea\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffe8e13f3\": {\r\n"
				+ "				\"friendly_name\": \"Bad 2\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe9655b4\": {\r\n"
				+ "				\"friendly_name\": \"Stehlampe Lennard 1\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe9700d6\": {\r\n"
				+ "				\"friendly_name\": \"Bewegungsmelder Flur EG\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe9a3df6\": {\r\n" + "				\"friendly_name\": \"Schreibtisch\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffe9d2e5d\": {\r\n"
				+ "				\"friendly_name\": \"Bad 1\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe9defc5\": {\r\n"
				+ "				\"friendly_name\": \"Fernbedienung Lennard\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffe9f3e75\": {\r\n" + "				\"friendly_name\": \"Haus Lennard\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffea4c8e9\": {\r\n"
				+ "				\"friendly_name\": \"SD1\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffea7f927\": {\r\n"
				+ "				\"friendly_name\": \"Deckenleuchte Lennard\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffeb283c9\": {\r\n"
				+ "				\"friendly_name\": \"Deckenleuchte Arbeitszimmer\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffebee945\": {\r\n" + "				\"friendly_name\": \"SD4\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffebeee14\": {\r\n"
				+ "				\"friendly_name\": \"SD3\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffedbe667\": {\r\n"
				+ "				\"friendly_name\": \"Fernbedienung Schlafzimmer\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffee2cb16\": {\r\n" + "				\"friendly_name\": \"Bad 6\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffeebd704\": {\r\n"
				+ "				\"friendly_name\": \"Bad 5\"\r\n" + "			},\r\n"
				+ "			\"0x000b57fffeec640e\": {\r\n" + "				\"friendly_name\": \"Bad 3\"\r\n"
				+ "			},\r\n" + "			\"0x000b57fffeec6a8b\": {\r\n"
				+ "				\"friendly_name\": \"Bad 4\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe1bc5b2\": {\r\n"
				+ "				\"friendly_name\": \"Fernbedienung Wohnzimmer\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe2ad5cd\": {\r\n"
				+ "				\"friendly_name\": \"RGB Arbeitszimmer Steckdose\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe2ade65\": {\r\n" + "				\"friendly_name\": \"Beamer\"\r\n"
				+ "			},\r\n" + "			\"0x000d6ffffe9a9f95\": {\r\n"
				+ "				\"friendly_name\": \"Lautsprecher\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe9aa4f1\": {\r\n"
				+ "				\"friendly_name\": \"Fernseher Lennard\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe9c22b6\": {\r\n" + "				\"friendly_name\": \"Spiegel\"\r\n"
				+ "			},\r\n" + "			\"0x000d6ffffe9c601e\": {\r\n"
				+ "				\"friendly_name\": \"Drucker\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffe9f3582\": {\r\n" + "				\"friendly_name\": \"PS4\"\r\n"
				+ "			},\r\n" + "			\"0x000d6ffffea03c07\": {\r\n"
				+ "				\"friendly_name\": \"Repeater EG\"\r\n" + "			},\r\n"
				+ "			\"0x000d6ffffea5c76d\": {\r\n" + "				\"friendly_name\": \"Stehlampe\"\r\n"
				+ "			},\r\n" + "			\"0x000d6ffffeb2c4d1\": {\r\n"
				+ "				\"friendly_name\": \"FB Arbeitszimmer\"\r\n" + "			},\r\n"
				+ "			\"0x00158d00038c0f82\": {\r\n"
				+ "				\"friendly_name\": \"0x00158d00038c0f82\"\r\n" + "			},\r\n"
				+ "			\"0x00158d00038c0fe8\": {\r\n"
				+ "				\"friendly_name\": \"Flur Keller links oben\"\r\n" + "			},\r\n"
				+ "			\"0x00158d000451907f\": {\r\n"
				+ "				\"friendly_name\": \"Bewegungsmelder Wohnzimmer\"\r\n" + "			},\r\n"
				+ "			\"0x00158d00047ea551\": {\r\n" + "				\"friendly_name\": \"Wasser Küche\"\r\n"
				+ "			},\r\n" + "			\"0x001788011025893c\": {\r\n"
				+ "				\"friendly_name\": \"0x001788011025893c\"\r\n" + "			},\r\n"
				+ "			\"0x00178801105c2f4e\": {\r\n"
				+ "				\"friendly_name\": \"Stehlampe Schlafzimmer\"\r\n" + "			},\r\n"
				+ "			\"0x14b457fffefaefc2\": {\r\n"
				+ "				\"friendly_name\": \"0x14b457fffefaefc2\"\r\n" + "			},\r\n"
				+ "			\"0x14b457fffefaeffa\": {\r\n"
				+ "				\"friendly_name\": \"0x14b457fffefaeffa\"\r\n" + "			},\r\n"
				+ "			\"0x588e81fffe6000eb\": {\r\n"
				+ "				\"friendly_name\": \"0x588e81fffe6000eb\"\r\n" + "			},\r\n"
				+ "			\"0x588e81fffe6bc268\": {\r\n"
				+ "				\"friendly_name\": \"0x588e81fffe6bc268\"\r\n" + "			},\r\n"
				+ "			\"0x588e81fffebcc097\": {\r\n"
				+ "				\"friendly_name\": \"0x588e81fffebcc097\"\r\n" + "			},\r\n"
				+ "			\"0x680ae2fffe32e4a6\": {\r\n"
				+ "				\"friendly_name\": \"Bewegungsmelder Flur OG\"\r\n" + "			},\r\n"
				+ "			\"0x680ae2fffe3cf2e4\": {\r\n"
				+ "				\"friendly_name\": \"Dimmer Treppenhaus\"\r\n" + "			},\r\n"
				+ "			\"0x680ae2fffe3f0573\": {\r\n"
				+ "				\"friendly_name\": \"0x680ae2fffe3f0573\"\r\n" + "			},\r\n"
				+ "			\"0x680ae2fffe6fd2d5\": {\r\n"
				+ "				\"friendly_name\": \"Rollo Treppenhaus\"\r\n" + "			},\r\n"
				+ "			\"0x680ae2fffef8187a\": {\r\n"
				+ "				\"friendly_name\": \"Schreibtisch Mandy\"\r\n" + "			},\r\n"
				+ "			\"0x7cb03eaa00b0b89e\": {\r\n" + "				\"friendly_name\": \"Plug 01\"\r\n"
				+ "			},\r\n" + "			\"0x7cb03eaa00b0cd53\": {\r\n"
				+ "				\"friendly_name\": \"Plug 02\"\r\n" + "			},\r\n"
				+ "			\"0xd0cf5efffe159a45\": {\r\n" + "				\"friendly_name\": \"Couch 5\"\r\n"
				+ "			},\r\n" + "			\"0xd0cf5efffe162327\": {\r\n"
				+ "				\"friendly_name\": \"Couch 4\"\r\n" + "			},\r\n"
				+ "			\"0xd0cf5efffe22aafe\": {\r\n" + "				\"friendly_name\": \"Couch 3\"\r\n"
				+ "			},\r\n" + "			\"0xd0cf5efffe2bcb0e\": {\r\n"
				+ "				\"friendly_name\": \"Couch 2\"\r\n" + "			},\r\n"
				+ "			\"0xd0cf5efffe2d37fb\": {\r\n" + "				\"friendly_name\": \"Couch 1\"\r\n"
				+ "			},\r\n" + "			\"0xec1bbdfffe85e485\": {\r\n"
				+ "				\"friendly_name\": \"Arbeitsleuchte\"\r\n" + "			},\r\n"
				+ "			\"0xec1bbdfffea85228\": {\r\n"
				+ "				\"friendly_name\": \"Bewegungsmelder Arbeitszimmer\"\r\n" + "			}\r\n"
				+ "		},\r\n" + "		\"experimental\": {\r\n" + "			\"new_api\": true,\r\n"
				+ "			\"output\": \"json\"\r\n" + "		},\r\n" + "		\"external_converters\": [],\r\n"
				+ "		\"frontend\": {\r\n" + "			\"port\": 8080\r\n" + "		},\r\n"
				+ "		\"groups\": {\r\n" + "			\"1\": {\r\n" + "				\"devices\": [\r\n"
				+ "					\"0xd0cf5efffe2bcb0e\",\r\n" + "					\"0xd0cf5efffe159a45\",\r\n"
				+ "					\"0xd0cf5efffe2d37fb\",\r\n" + "					\"0xd0cf5efffe22aafe\",\r\n"
				+ "					\"0xd0cf5efffe162327\"\r\n" + "				],\r\n"
				+ "				\"friendly_name\": \"Couchlampe\",\r\n" + "				\"optimistic\": true,\r\n"
				+ "				\"retain\": false,\r\n" + "				\"transition\": 2\r\n" + "			},\r\n"
				+ "			\"2\": {\r\n" + "				\"devices\": [\r\n"
				+ "					\"0xd0cf5efffe2bcb0e\",\r\n" + "					\"0xd0cf5efffe159a45\",\r\n"
				+ "					\"0xd0cf5efffe2d37fb\",\r\n" + "					\"0xd0cf5efffe22aafe\",\r\n"
				+ "					\"0xd0cf5efffe162327\"\r\n" + "				],\r\n"
				+ "				\"friendly_name\": \"Couchlampe2\",\r\n" + "				\"optimistic\": true,\r\n"
				+ "				\"retain\": false,\r\n" + "				\"transition\": 2\r\n" + "			}\r\n"
				+ "		},\r\n" + "		\"homeassistant\": true,\r\n" + "		\"map_options\": {\r\n"
				+ "			\"graphviz\": {\r\n" + "				\"colors\": {\r\n"
				+ "					\"fill\": {\r\n" + "						\"coordinator\": \"#e04e5d\",\r\n"
				+ "						\"enddevice\": \"#fff8ce\",\r\n"
				+ "						\"router\": \"#4ea3e0\"\r\n" + "					},\r\n"
				+ "					\"font\": {\r\n" + "						\"coordinator\": \"#ffffff\",\r\n"
				+ "						\"enddevice\": \"#000000\",\r\n"
				+ "						\"router\": \"#ffffff\"\r\n" + "					},\r\n"
				+ "					\"line\": {\r\n" + "						\"active\": \"#009900\",\r\n"
				+ "						\"inactive\": \"#994444\"\r\n" + "					}\r\n"
				+ "				}\r\n" + "			}\r\n" + "		},\r\n" + "		\"mqtt\": {\r\n"
				+ "			\"base_topic\": \"zigbee2mqtt\",\r\n" + "			\"discovery\": true,\r\n"
				+ "			\"include_device_information\": false,\r\n" + "			\"server\": \"mqtt://nas\"\r\n"
				+ "		},\r\n" + "		\"passlist\": [],\r\n" + "		\"permit_join\": true,\r\n"
				+ "		\"serial\": {\r\n" + "			\"disable_led\": false,\r\n"
				+ "			\"port\": \"/dev/ttyACM0\"\r\n" + "		},\r\n" + "		\"whitelist\": []\r\n" + "	},\r\n"
				+ "	\"coordinator\": {\r\n" + "		\"meta\": {\r\n" + "			\"maintrel\": 3,\r\n"
				+ "			\"majorrel\": 2,\r\n" + "			\"minorrel\": 6,\r\n" + "			\"product\": 0,\r\n"
				+ "			\"revision\": 20190608,\r\n" + "			\"transportrev\": 2\r\n" + "		},\r\n"
				+ "		\"type\": \"zStack12\"\r\n" + "	},\r\n" + "	\"log_level\": \"info\",\r\n"
				+ "	\"network\": {\r\n" + "		\"channel\": 11,\r\n"
				+ "		\"extended_pan_id\": \"0xdddddddddddddddd\",\r\n" + "		\"pan_id\": 6754\r\n" + "	},\r\n"
				+ "	\"permit_join\": true,\r\n" + "	\"version\": \"1.16.1\"\r\n" + "}";
		zigbeeMQTTReceiver.handleBridgeInfo(message);
	}
}
