package cm.homeautomation.admin;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.Room;

@XmlRootElement
public class RoomList {
	private List<Room> rooms;

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
}
