package cm.homeautomation.logging;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WebSocketEvent {

	private String message;

	public WebSocketEvent(String message) {
		this.setMessage(message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
