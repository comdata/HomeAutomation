package cm.homeautomation.ebus;

import java.util.Date;

import javax.persistence.NoResultException;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EBUSStatus01Receiver {

	private static final String SOLARPUMP = "SOLARPUMP";
	private static final String SOLARBACKTEMP = "SOLARBACKTEMP";
	private static final String WATERPRESSURE = "WATERPRESSURE";
	private static final String EBUSD_BAI_SOL_PUMP = "ebusd/bai/SolPump";
	private static final String EBUSD_BAI_SOL_BACK_TEMP = "ebusd/bai/SolBackTemp";
	private static final String EBUSD_BAI_WATER_PRESSURE = "ebusd/bai/WaterPressure";
	private static final String EBUSD_BAI_STATUS01 = "ebusd/bai/Status01";
	private static final String SEMICOLON = ";";
	private static final String OFF = "off";
	private static final String ON = "on";
	private static final String PUMPSTATE = "PUMPSTATE";
	private static final String STORAGETEMP = "STORAGETEMP";
	private static final String WARMWATERTEMP = "WARMWATERTEMP";
	private static final String OUTSIDETEMP = "OUTSIDETEMP";
	private static final String RETURNTEMP = "RETURNTEMP";
	private static final String HEATINGTEMP = "HEATINGTEMP";
	private static final String FLAME = "FLAME";
	private static final String SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK = "Sensors class not initialized correctly. Got no instance back.";
	private static final String ERROR_SAVING_EBUS_DATA = "Error saving ebus data";
	private static final String STORED_EBUS_VALUE_S_FOR_S = "Stored ebus value: {} for: {}";
	private static final String SENSOR_NOT_DEFINED_FOR_S = "Sensor not defined for: {}";
	private static final String EMPTY_EVENT_RECEIVED = "EBUS Empty event received.";

	public EBUSStatus01Receiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receive(EventObject eventObject) {
		if (eventObject.getData() instanceof EBusMessageEvent) {
			EBusMessageEvent messageEvent = (EBusMessageEvent) eventObject.getData();
			Sensors sensorsInstance = Sensors.getInstance();
			
			System.out.println("EBUS topic "+ messageEvent.getTopic());
			System.out.println("EBUS message content: "+messageEvent.getMessageContent());

			if (EBUSD_BAI_STATUS01.equals(messageEvent.getTopic())) {
				String[] technicalNames = { HEATINGTEMP, RETURNTEMP, OUTSIDETEMP, WARMWATERTEMP, STORAGETEMP,
						PUMPSTATE };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace(ON, "1").replace(OFF, "0");

				String[] valueParts = messageString.split(SEMICOLON);
				for (int i = 0; i < 6; i++) {
					String sensorValue = valueParts[i];

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
							log.error(STORED_EBUS_VALUE_S_FOR_S, sensorValue, technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							log.error(SENSOR_NOT_DEFINED_FOR_S, technicalNames[i]);

						} catch (Exception e) {
							log.error(ERROR_SAVING_EBUS_DATA, e);

						}
					} else {
						log.error(SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK);

					}
				}

			} else if (EBUSD_BAI_WATER_PRESSURE.equals(messageEvent.getTopic())) {
				String[] technicalNames = { WATERPRESSURE };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace(ON, "1").replace(OFF, "0");

				String[] valueParts = messageString.split(SEMICOLON);
				for (int i = 0; i < 1; i++) {

					String sensorValue = valueParts[i];
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
							log.error(STORED_EBUS_VALUE_S_FOR_S, sensorValue, technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							log.error(SENSOR_NOT_DEFINED_FOR_S, technicalNames[i]);

						} catch (Exception e) {
							log.error(ERROR_SAVING_EBUS_DATA, e);

						}
					} else {
						log.error(SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK);

					}
				}
			} else if (EBUSD_BAI_SOL_BACK_TEMP.equals(messageEvent.getTopic())) {
				String[] technicalNames = { SOLARBACKTEMP };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace(ON, "1").replace(OFF, "0");

				String[] valueParts = messageString.split(SEMICOLON);
				for (int i = 0; i < 1; i++) {

					String sensorValue = valueParts[i];
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
							log.error(STORED_EBUS_VALUE_S_FOR_S, sensorValue, technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							log.error(SENSOR_NOT_DEFINED_FOR_S, technicalNames[i]);

						} catch (Exception e) {
							log.error(ERROR_SAVING_EBUS_DATA, e);

						}
					} else {
						log.error(SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK);

					}
				}
			} else if (EBUSD_BAI_SOL_PUMP.equals(messageEvent.getTopic())) {
				String[] technicalNames = { SOLARPUMP };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace(ON, "1").replace(OFF, "0");

				String[] valueParts = messageString.split(SEMICOLON);
				for (int i = 0; i < 1; i++) {

					String sensorValue = valueParts[i];
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
							log.error(STORED_EBUS_VALUE_S_FOR_S, sensorValue, technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							log.error(SENSOR_NOT_DEFINED_FOR_S, technicalNames[i]);

						} catch (Exception e) {
							log.error(ERROR_SAVING_EBUS_DATA, e);

						}
					} else {
						log.error(SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK);

					}
				}
			} else if ("ebusd/bai/Flame".equals(messageEvent.getTopic())) {
				String[] technicalNames = { FLAME };
				String messageString = messageEvent.getMessageContent();
				messageString = messageString.replace(ON, "1").replace(OFF, "0");

				String[] valueParts = messageString.split(SEMICOLON);
				for (int i = 0; i < 1; i++) {

					String sensorValue = valueParts[i];
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
							log.error(STORED_EBUS_VALUE_S_FOR_S, sensorValue, technicalNames[i]);
							sensorsInstance.saveSensorData(sensorDataSaveRequest);
						} catch (NoResultException e) {
							log.error(SENSOR_NOT_DEFINED_FOR_S, technicalNames[i]);

						} catch (Exception e) {
							log.error(ERROR_SAVING_EBUS_DATA, e);

						}
					} else {
						log.error(SENSORS_CLASS_NOT_INITIALIZED_CORRECTLY_GOT_NO_INSTANCE_BACK);

					}
				}
			}
		} else {
			log.error(EMPTY_EVENT_RECEIVED);
		}
	}
}
