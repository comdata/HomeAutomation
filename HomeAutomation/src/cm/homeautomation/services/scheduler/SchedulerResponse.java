package cm.homeautomation.services.scheduler;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SchedulerResponse {

	private boolean success=false;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
