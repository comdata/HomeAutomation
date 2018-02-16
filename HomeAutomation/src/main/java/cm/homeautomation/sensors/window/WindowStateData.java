package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.persistence.Transient;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Window;

public class WindowStateData {
	private int state;
	private String mac;
	private Room room;

	private Window window;

	@Transient
	private Device device;
	private Date date;

	public Date getDate() {
		return date;
	}

	public Device getDevice() {
		return device;
	}

	public String getMac() {
		return mac;
	}

	public Room getRoom() {
		return room;
	}

	public int getState() {
		return state;
	}

	public Window getWindow() {
		return window;
	}

	public void setDate(Date date) {
		this.date = date;

	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

}
