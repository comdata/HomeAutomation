package cm.homeautomation.eventbus;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;


@XmlRootElement
@JsonInclude (JsonInclude.Include.USE_DEFAULTS)
public class EventObject {

	private Object data;
	private String clazz;
	private String packageName;
	
	public EventObject(Object data) {
		setData(data);
	}

	public Object getData() {
			
		return data;
	}

	public void setData(Object data) {
		if (data!=null) {
			setClazz(data.getClass().getSimpleName());
			setPackageName(data.getClass().getPackage().getName());
		}
		
		this.data = data;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getPackageName() {
		return packageName;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
}
