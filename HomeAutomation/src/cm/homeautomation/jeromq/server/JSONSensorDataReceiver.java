package cm.homeautomation.jeromq.server;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.DistanceSensorData;
import cm.homeautomation.sensors.GasmeterData;
import cm.homeautomation.sensors.JSONSensorDataBase;
import cm.homeautomation.sensors.PowerMeterData;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.WindowSensorData;
import cm.homeautomation.services.sensors.Sensors;

/**
 * JSON Data Receiver and mapper
 * 
 * forwards messages to the EventBus
 * 
 * @author christoph
 *
 */
public class JSONSensorDataReceiver {

	public JSONSensorDataReceiver() {
		// intentionally left empty
	}

	public static void receiveSensorData(String messageContent) {

		try {
			if (!messageContent.contains("@c") ) {
				return;
			}
			
			
			Sensors sensorsService = new Sensors();
			
			ObjectMapper mapper = new ObjectMapper();
			
			messageContent=messageContent.replace("@class", "@c");
			
			messageContent=messageContent.replace("cm.homeautomation.sensors", "");
			
			messageContent=messageContent.replace("cm.homeautomation.transmission.TransmissionStatusData", ".TransmissionStatusData");
			
			LogManager.getLogger(JSONSensorDataReceiver.class).info("message for deserialization: "+messageContent);
			
			JSONSensorDataBase sensorData = mapper.readValue(messageContent, JSONSensorDataBase.class);

			if (sensorData instanceof SensorDataSaveRequest) {
				sensorsService.saveSensorData((SensorDataSaveRequest) sensorData);
			} else if (sensorData instanceof SensorDataRoomSaveRequest) {
				sensorsService.save((SensorDataRoomSaveRequest) sensorData);
			} else if (sensorData instanceof DistanceSensorData) {
				EventObject eventObject=new EventObject((DistanceSensorData)sensorData);
				EventBusService.getEventBus().post(eventObject);	
			} else if (sensorData instanceof PowerMeterData) {
				EventObject eventObject=new EventObject((PowerMeterData)sensorData);
				EventBusService.getEventBus().post(eventObject);	
			}else if (sensorData instanceof GasmeterData) {
				EventObject eventObject=new EventObject((GasmeterData)sensorData);
				EventBusService.getEventBus().post(eventObject);	
			}
			
			else if (sensorData instanceof WindowSensorData) {
				EventObject eventObject=new EventObject((WindowSensorData)sensorData);
				EventBusService.getEventBus().post(eventObject);	
			}
		} catch (IOException e) {
			LogManager.getLogger(JSONSensorDataReceiver.class).error("received IOException", e);
		}
	}
}