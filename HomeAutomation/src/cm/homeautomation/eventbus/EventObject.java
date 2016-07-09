package cm.homeautomation.eventbus;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventObject {

	private Object data;
	private String clazz;
	
	public EventObject(Object data) {
		setData(data);
	}

	public Object getData() {
			
		return data;
	}

	public void setData(Object data) {
		if (data!=null) {
			setClazz(data.getClass().getSimpleName());
		}
		
		this.data = data;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
}
