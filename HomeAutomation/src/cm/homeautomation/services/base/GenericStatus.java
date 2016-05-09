package cm.homeautomation.services.base;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericStatus {
	
	
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
}
