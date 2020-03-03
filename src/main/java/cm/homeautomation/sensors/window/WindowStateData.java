package cm.homeautomation.sensors.window;

import java.util.Date;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Window;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Getter;
import lombok.Setter;

public class WindowStateData implements HumanMessageGenerationInterface {
	private int state;
	private String mac;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	@EdmIgnore
	private Room room;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("window")
	@ManyToOne
	@JoinColumn(name = "WINDOW_ID")
	@EdmIgnore
	private Window window;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("device")
	@ManyToOne
	@JoinColumn(name = "DEVICE_ID")
	@EdmIgnore
	private Device device;
	private Date date;
	
	@Getter
	@Setter
	private String roomName;
	
	@Getter
	@Setter
	private String windowName;

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
