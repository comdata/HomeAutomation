package cm.homeautomation.services.actor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.Switch;

@XmlRootElement
public class SwitchStatuses {
	private List<Switch> switchStatuses;

	public List<Switch> getSwitchStatuses() {
		if (switchStatuses==null) {
			switchStatuses=new ArrayList<>();
		}
		return switchStatuses;
	}

	public void setSwitchStatuses(List<Switch> switchStatuses) {
		this.switchStatuses = switchStatuses;
	}
	
	
}
