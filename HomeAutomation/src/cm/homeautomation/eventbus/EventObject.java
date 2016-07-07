package cm.homeautomation.eventbus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventObject {

	private Object data;
	
	public EventObject(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
