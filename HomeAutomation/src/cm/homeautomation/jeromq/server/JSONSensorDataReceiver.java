package cm.homeautomation.jeromq.server;

import java.io.IOException;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;

import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.sensors.JSONSensorDataBase;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
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
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}