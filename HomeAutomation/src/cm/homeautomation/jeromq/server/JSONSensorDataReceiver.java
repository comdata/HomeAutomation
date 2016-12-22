package cm.homeautomation.jeromq.server;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.DistanceSensorData;
import cm.homeautomation.sensors.JSONSensorDataBase;
import cm.homeautomation.sensors.PowerMeterData;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.window.WindowSensorData;
import cm.homeautomation.services.sensors.Sensors;

public class JSONSensorDataReceiver {

	public JSONSensorDataReceiver() {
	}

	public static void receiveSensorData(String messageContent) {

		try {
			Sensors sensorsService = new Sensors();
			
			ObjectMapper mapper = new ObjectMapper();
			
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
			}
			
			else if (sensorData instanceof WindowSensorData) {
				EventObject eventObject=new EventObject((WindowSensorData)sensorData);
				EventBusService.getEventBus().post(eventObject);	
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}