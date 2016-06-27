package cm.homeautomation.services.sensors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SensorDataRoomSaveRequest {
private Long roomID;
private SensorDataRoomPayload data;
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
}
