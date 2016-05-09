package cm.homeautomation.eventbus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventObject {

	private String eventName;
	private Object data;
	
	public EventObject(String eventName, Object data) {
		this.eventName = eventName;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}
