package cm.homeautomation.services.overview;

import javax.xml.bind.annotation.XmlRootElement;

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
	private String eventHandler="handleSelect";
	private String tileType;
	
   /* "icon" : "inbox",
    "number" : "89",
    "title" : "Approve Leave Requests",
    "info" : "Overdue",
    "infoState" : "Error"*/
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getNumberUnit() {
		return numberUnit;
	}
	public void setNumberUnit(String numberUnit) {
		this.numberUnit = numberUnit;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getInfoState() {
		return infoState;
	}
	public void setInfoState(String infoState) {
		this.infoState = infoState;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	
	public String getRoomName() {
		return roomName;
	}
	public String getEventHandler() {
		return eventHandler;
	}
	public void setEventHandler(String eventHandler) {
		this.eventHandler = eventHandler;
	}
	public String getTileType() {
		return tileType;
	}
	public void setTileType(String tileType) {
		this.tileType = tileType;
	}
}
