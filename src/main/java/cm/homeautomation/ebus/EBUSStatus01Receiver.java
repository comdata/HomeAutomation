package cm.homeautomation.ebus;

import java.util.Date;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;

public class EBUSStatus01Receiver {

	public EBUSStatus01Receiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receive(EventObject eventObject) {
		if (eventObject.getData() instanceof EBusMessageEvent) {
			EBusMessageEvent messageEvent = (EBusMessageEvent) eventObject.getData();

			if ("ebusd/bai/Status01".equals(messageEvent.getTopic())) {
				String[] technicalNames = { "HEATINGTEMP", "RETURNTEMP", "OUTSIDETEMP", "WARMWATERTEMP", "STORAGETEMP",
						"PUMPSTATE" };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace("on", "1").replace("off", "0");

				Sensors sensorsInstance = Sensors.getInstance();
				for (int i = 0; i < 6; i++) {

					String sensorValue = messageString.split(";")[i];
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					SensorData sensorData = new SensorData();
					sensorData.setValue(sensorValue);
					sensorData.setDateTime(new Date());
					Sensor sensor = new Sensor();
					sensor.setSensorName(technicalNames[i]);
					sensor.setSensorTechnicalType(technicalNames[i]);
					sensorData.setSensor(sensor);
					sensorDataSaveRequest.setSensorData(sensorData);

					if (sensorsInstance != null) {
						try {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Stored ebus value: " + sensorValue + " for: " + technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Sensor not defined for: " + technicalNames[i]);

						} catch (Exception e) {
							LogManager.getLogger(EBUSStatus01Receiver.class).error("Error saving ebus data", e);

						}
					} else {
						LogManager.getLogger(EBUSStatus01Receiver.class)
								.error("Sensors class not initialized correctly. Got no instance back.");

					}
				}

			} else if ("ebusd/bai/WaterPressure".equals(messageEvent.getTopic())) {
				String[] technicalNames = { "WATERPRESSURE" };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace("on", "1").replace("off", "0");

				Sensors sensorsInstance = Sensors.getInstance();
				for (int i = 0; i < 1; i++) {

					String sensorValue = messageString.split(";")[i];
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					SensorData sensorData = new SensorData();
					sensorData.setValue(sensorValue);
					sensorData.setDateTime(new Date());
					Sensor sensor = new Sensor();
					sensor.setSensorName(technicalNames[i]);
					sensor.setSensorTechnicalType(technicalNames[i]);
					sensorData.setSensor(sensor);
					sensorDataSaveRequest.setSensorData(sensorData);

					if (sensorsInstance != null) {
						try {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Stored ebus value: " + sensorValue + " for: " + technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Sensor not defined for: " + technicalNames[i]);

						} catch (Exception e) {
							LogManager.getLogger(EBUSStatus01Receiver.class).error("Error saving ebus data", e);

						}
					} else {
						LogManager.getLogger(EBUSStatus01Receiver.class)
								.error("Sensors class not initialized correctly. Got no instance back.");

					}
				}
			} else if ("ebusd/bai/SolBackTemp".equals(messageEvent.getTopic())) {
				String[] technicalNames = { "SOLARBACKTEMP" };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace("on", "1").replace("off", "0");

				Sensors sensorsInstance = Sensors.getInstance();
				for (int i = 0; i < 1; i++) {

					String sensorValue = messageString.split(";")[i];
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					SensorData sensorData = new SensorData();
					sensorData.setValue(sensorValue);
					sensorData.setDateTime(new Date());
					Sensor sensor = new Sensor();
					sensor.setSensorName(technicalNames[i]);
					sensor.setSensorTechnicalType(technicalNames[i]);
					sensorData.setSensor(sensor);
					sensorDataSaveRequest.setSensorData(sensorData);

					if (sensorsInstance != null) {
						try {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Stored ebus value: " + sensorValue + " for: " + technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Sensor not defined for: " + technicalNames[i]);

						} catch (Exception e) {
							LogManager.getLogger(EBUSStatus01Receiver.class).error("Error saving ebus data", e);

						}
					} else {
						LogManager.getLogger(EBUSStatus01Receiver.class)
								.error("Sensors class not initialized correctly. Got no instance back.");

					}
				}
			} else if ("ebusd/bai/SolPump".equals(messageEvent.getTopic())) {
				String[] technicalNames = { "SOLARPUMP" };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace("on", "1").replace("off", "0");

				Sensors sensorsInstance = Sensors.getInstance();
				for (int i = 0; i < 1; i++) {

					String sensorValue = messageString.split(";")[i];
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					SensorData sensorData = new SensorData();
					sensorData.setValue(sensorValue);
					sensorData.setDateTime(new Date());
					Sensor sensor = new Sensor();
					sensor.setSensorName(technicalNames[i]);
					sensor.setSensorTechnicalType(technicalNames[i]);
					sensorData.setSensor(sensor);
					sensorDataSaveRequest.setSensorData(sensorData);

					if (sensorsInstance != null) {
						try {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Stored ebus value: " + sensorValue + " for: " + technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Sensor not defined for: " + technicalNames[i]);

						} catch (Exception e) {
							LogManager.getLogger(EBUSStatus01Receiver.class).error("Error saving ebus data", e);

						}
					} else {
						LogManager.getLogger(EBUSStatus01Receiver.class)
								.error("Sensors class not initialized correctly. Got no instance back.");

					}
				}
			}else if ("ebusd/bai/Flame".equals(messageEvent.getTopic())) {
				String[] technicalNames = { "FLAME" };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace("on", "1").replace("off", "0");

				Sensors sensorsInstance = Sensors.getInstance();
				for (int i = 0; i < 1; i++) {

					String sensorValue = messageString.split(";")[i];
					SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
					SensorData sensorData = new SensorData();
					sensorData.setValue(sensorValue);
					sensorData.setDateTime(new Date());
					Sensor sensor = new Sensor();
					sensor.setSensorName(technicalNames[i]);
					sensor.setSensorTechnicalType(technicalNames[i]);
					sensorData.setSensor(sensor);
					sensorDataSaveRequest.setSensorData(sensorData);

					if (sensorsInstance != null) {
						try {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Stored ebus value: " + sensorValue + " for: " + technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							LogManager.getLogger(EBUSStatus01Receiver.class)
									.error("Sensor not defined for: " + technicalNames[i]);

						} catch (Exception e) {
							LogManager.getLogger(EBUSStatus01Receiver.class).error("Error saving ebus data", e);

						}
					} else {
						LogManager.getLogger(EBUSStatus01Receiver.class)
								.error("Sensors class not initialized correctly. Got no instance back.");

					}
				}
			}
		} else {
			LogManager.getLogger(EBUSStatus01Receiver.class).error("Empty event received.");
		}
	}
}
