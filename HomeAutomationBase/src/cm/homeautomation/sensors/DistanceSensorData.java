package cm.homeautomation.sensors;

public class DistanceSensorData extends JSONSensorDataBase {

	private Long roomID;
	private String distance;
	public Long getRoomID() {
		return roomID;
	}
	public void setRoomID(Long roomID) {
		this.roomID = roomID;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
}
