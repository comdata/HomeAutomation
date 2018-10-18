package cm.homeautomation.services.overview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;

@XmlRootElement
public class OverviewTile {

	private String roomId;
	private String icon;
	private String number;
	private String numberUnit;
	private String title;
	private String info;
	private String infoState;
	private String roomName;
	private String eventHandler = "handleSelect";
	private String tileType;

	@JsonIgnore
	private Room room;

	@JsonIgnore
	private Map<Sensor, SensorData> sensorData;
	@JsonIgnore
	private Set<Switch> switches;

	public String getEventHandler() {
		return eventHandler;
	}

	/*
	 * "icon" : "inbox", "number" : "89", "title" : "Approve Leave Requests", "info"
	 * : "Overdue", "infoState" : "Error"
	 */
	public String getIcon() {
		return icon;
	}

	public String getInfo() {
		return info;
	}

	public String getInfoState() {
		return infoState;
	}

	public String getNumber() {
		return number;
	}

	public String getNumberUnit() {
		return numberUnit;
	}

	@JsonIgnore
	public Room getRoom() {
		return room;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getRoomName() {
		return roomName;
	}

	public Map<Sensor, SensorData> getSensorData() {
		if (sensorData == null) {
			sensorData = new HashMap<>();
		}

		return sensorData;
	}

	@JsonIgnore
	public Set<Switch> getSwitches() {

		if (switches == null) {
			switches = new HashSet<>();
		}

		return switches;
	}

	public String getTileType() {
		return tileType;
	}

	public String getTitle() {
		return title;
	}

	public void setEventHandler(String eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void setInfoState(String infoState) {
		this.infoState = infoState;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setNumberUnit(String numberUnit) {
		this.numberUnit = numberUnit;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public void setSensorData(Map<Sensor, SensorData> sensorData) {
		this.sensorData = sensorData;
	}

	public void setSwitches(Set<Switch> switches) {
		this.switches = switches;
	}

	public void setTileType(String tileType) {
		this.tileType = tileType;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
