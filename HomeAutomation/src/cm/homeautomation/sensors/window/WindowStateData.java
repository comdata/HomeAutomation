package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.persistence.Transient;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class WindowStateData {
	private int state;
	private String mac;
	private Room room;
	
	@Transient
	private Device device;
	private Date date;

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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setDate(Date date) {
		this.date = date;

	}
	
	public Date getDate() {
		return date;
	}
	
}
