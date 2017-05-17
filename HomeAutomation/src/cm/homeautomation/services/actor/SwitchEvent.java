package cm.homeautomation.services.actor;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.Switch;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

@XmlRootElement
public class SwitchEvent implements HumanMessageGenerationInterface {
	private String switchId;
	private String status;
	private Switch usedSwitch;

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

	@Override
	public String getMessageString() {
		return "Switch "+getUsedSwitch().getName()+" switched to "+getStatus();
	}

	public void setUsedSwitch(Switch singleSwitch) {
		this.usedSwitch = singleSwitch;
	}
	
	@Transient
	public Switch getUsedSwitch() {
		return usedSwitch;
	}
}
