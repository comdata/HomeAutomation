package cm.homeautomation.jeromq.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.DistanceSensorData;
import cm.homeautomation.sensors.GasmeterData;
import cm.homeautomation.sensors.IRData;
import cm.homeautomation.sensors.JSONSensorDataBase;
import cm.homeautomation.sensors.PowerMeterData;
import cm.homeautomation.sensors.RainData;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.WindowSensorData;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import io.quarkus.runtime.StartupEvent;

/**
 * JSON Data Receiver and mapper
 * 
 * forwards messages to the EventBus
 * 
 * @author christoph
 *
 */
@Singleton
public class JSONSensorDataReceiver {

	private static final String RECEIVED_SENSOR_DATA_LIMIT_VIOLATION_EXCEPTION = "received SensorDataLimitViolationException";
	private static final String RECEIVED_IO_EXCEPTION = "received IOException";
	private static final String MESSAGE_FOR_DESERIALIZATION_S = "message for deserialization: {}";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Sensors sensorsService = new Sensors();

	public JSONSensorDataReceiver() {
		EventBusService.getEventBus().register(this);
	}

	void startup(@Observes StartupEvent event) {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void receiveSensorData(JSONDataEvent jsonDataEvent) {

		String messageContent = jsonDataEvent.getMessage();

		try {
			if (!messageContent.contains("@c")) {
				return;
			}

			messageContent = filterMessageContent(messageContent);

			if (messageContent.contains("\"}?")) {
				String[] split = messageContent.split("[?]");
				messageContent = split[0];
			}

//            LogManager.getLogger(JSONSensorDataReceiver.class).info(MESSAGE_FOR_DESERIALIZATION_S, messageContent);

			JSONSensorDataBase sensorData = mapper.readValue(messageContent, JSONSensorDataBase.class);

			List<Class<?>> classList = new ArrayList<>();
			classList.add(DistanceSensorData.class);

			classList.add(DistanceSensorData.class);
			classList.add(PowerMeterData.class);
			classList.add(GasmeterData.class);
			classList.add(RainData.class);
			classList.add(WindowSensorData.class);
			classList.add(IRData.class);

			if (sensorData instanceof SensorDataSaveRequest) {
				sensorsService.saveSensorData((SensorDataSaveRequest) sensorData);
			} else if (sensorData instanceof SensorDataRoomSaveRequest) {
				sensorsService.save((SensorDataRoomSaveRequest) sensorData);
			} else {
				for (Class<?> clazz : classList) {
					if (clazz.isInstance(sensorData)) {
//                        LogManager.getLogger(JSONSensorDataReceiver.class).debug("Casting to: {}",
//                                clazz.getSimpleName());
						EventObject eventObject = new EventObject(clazz.cast(sensorData));
						EventBusService.getEventBus().post(eventObject);
					}
				}
			}
		} catch (IOException e) {
//            LogManager.getLogger(JSONSensorDataReceiver.class).error(RECEIVED_IO_EXCEPTION, e);
		} catch (SensorDataLimitViolationException e) {
//            LogManager.getLogger(JSONSensorDataReceiver.class).error(RECEIVED_SENSOR_DATA_LIMIT_VIOLATION_EXCEPTION, e);
		}
	}

	private static String filterMessageContent(String messageContent) {
		Map<String, String> map = new HashMap<>();
		map.put("@class", "@c");
		map.put("cm.homeautomation.sensors", "");
		map.put("cm.homeautomation.transmission.TransmissionStatusData", ".TransmissionStatusData");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			messageContent = messageContent.replace(entry.getKey(), entry.getValue());
		}
		return messageContent;
	}
}