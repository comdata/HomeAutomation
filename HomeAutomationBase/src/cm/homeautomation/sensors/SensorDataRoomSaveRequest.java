package cm.homeautomation.sensors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class SensorDataRoomSaveRequest extends JSONSensorDataBase {
	private Long roomID;
	private SensorDataRoomPayload data;
	
	// mac address of the sensor
	private String mac;

	public Long getRoomID() {
		return roomID;
	}

	public void setRoomID(Long roomID) {
		this.roomID = roomID;
	}

	public SensorDataRoomPayload getData() {
		return data;
	}

	public void setData(SensorDataRoomPayload data) {
		this.data = data;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}
}
