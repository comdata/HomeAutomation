package cm.homeautomation.services.messaging;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HumanMessageEvent {

	private String message;

	public HumanMessageEvent() {
		
	}
	
	public HumanMessageEvent(String message) {
		this.message = message;
		
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
