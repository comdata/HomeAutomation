package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.persistence.Transient;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Window;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class WindowStateData implements HumanMessageGenerationInterface {
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

	@Override
	public String getMessageString() {
		return "Raum: "+((room!=null)?room.getRoomName():"")+" Fenster: " + window.getName() + " Status: " + ((getState() == 0) ? "geschlossen" : "offen");
	}

	public Room getRoom() {
		return room;
	}

	public int getState() {
		return state;
	}

	@Override
	public String getTitle() {

		return "Window state changed";
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
