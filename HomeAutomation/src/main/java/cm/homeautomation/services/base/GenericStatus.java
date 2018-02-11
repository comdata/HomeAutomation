package cm.homeautomation.services.base;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericStatus {
	
	private Object object;
	private boolean success;

	public GenericStatus() {
		success=false;
	}
	
	public GenericStatus(boolean success) {
		this.success = success;
		
	}
	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
