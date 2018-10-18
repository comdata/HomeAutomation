package cm.homeautomation.services.motion;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

@XmlRootElement
public class MotionEvent implements HumanMessageGenerationInterface{

	private String mac;
	private int room;
	private boolean state;
	
	public MotionEvent() {
	}
	
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public int getRoom() {
		return room;
	}
	public void setRoom(int room) {
		this.room = room;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	@Override
	public String getMessageString() {
		if (state) {
			return "Motion detected. Room:" + getRoom() + " state: " + getState();
		} else {
			return "Motion stopped. Room:" + getRoom() + " state: " + getState();
		}
	}

	@Override
	public String getTitle() {
		return "MotionEvent";
	}
}
