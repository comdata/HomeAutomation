package cm.homeautomation.services.motion;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
public class MotionEvent implements HumanMessageGenerationInterface{

	private String mac;
	private int room;
	private boolean state;
	String name;
	
	@Override
	public String getMessageString() {
		if (state) {
			return "Motion detected. Room:" + getRoom() + " state: " + isState();
		} else {
			return "Motion stopped. Room:" + getRoom() + " state: " + isState();
		}
	}

	@Override
	public String getTitle() {
		return "MotionEvent";
	}
}
