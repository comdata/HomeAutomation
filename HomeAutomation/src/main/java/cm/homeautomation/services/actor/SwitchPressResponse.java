package cm.homeautomation.services.actor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SwitchPressResponse {
	private boolean success=false;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
