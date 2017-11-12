package cm.homeautomation.sensors;

import java.util.Date;

public class SensorDataRoomSaveRequest extends JSONSensorDataBase {
	private Long roomID;
	private SensorDataRoomPayload data;

	// mac address of the sensor
	private String mac;

	private Date timestamp;

	public SensorDataRoomPayload getData() {
		return data;
	}

	public String getMac() {
		return mac;
	}

	public Long getRoomID() {
		return roomID;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setData(final SensorDataRoomPayload data) {
		this.data = data;
	}

	public void setMac(final String mac) {
		this.mac = mac;
	}

	public void setRoomID(final Long roomID) {
		this.roomID = roomID;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}
}
