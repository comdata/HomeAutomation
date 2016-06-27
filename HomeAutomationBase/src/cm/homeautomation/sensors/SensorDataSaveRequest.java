package cm.homeautomation.sensors;

import cm.homeautomation.entities.SensorData;

public class SensorDataSaveRequest extends JSONSensorDataBase {
	private Long sensorId;
	private SensorData sensorData;
	
	public Long getSensorId() {
		return sensorId;
	}

	public void setSensorId(Long sensorId) {
		this.sensorId = sensorId;
	}

	public SensorData getSensorData() {
		return sensorData;
	}

	public void setSensorData(SensorData sensorData) {
		this.sensorData = sensorData;
	}
	
	
	
}
