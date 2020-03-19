package cm.homeautomation.admin;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.Room;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
public class RoomList {
	private List<Room> rooms;

}
