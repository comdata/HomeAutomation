package cm.homeautomation.sensors.window;

import cm.homeautomation.entities.Room;

public class WindowStateData {
	private int state;
	private String mac;
	private Room room;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}
	
}
