package cm.homeautomation.services.actor;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SwitchEvent {
	private String switchId;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSwitchId() {
		return switchId;
	}

	public void setSwitchId(String switchId) {
		this.switchId = switchId;
	}
}
