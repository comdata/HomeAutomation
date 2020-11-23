package cm.homeautomation.services.motion;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.Room;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
public class MotionEvent implements HumanMessageGenerationInterface{

	private String mac;
	private Room room;
	private boolean state;
    String name;
    private Date timestamp;
    
    private String type;
	
	@Override
	public String getMessageString() {
		String roomName = (getRoom()!=null) ? getRoom().getRoomName(): "not maintained";
		if (state) {
			return "Motion detected. Name: " + name + " Room:" + roomName + " state: " + isState();
		} else {
			return "Motion stopped. Name: " + name + " Room:" + roomName + " state: " + isState();
		}
	}

	@Override
	public String getTitle() {
		return "MotionEvent";
	}
}
