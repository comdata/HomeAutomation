package cm.homeautomation.zigbee;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class WindowContactEvent implements HumanMessageGenerationInterface {

	String device;
	String id;
	
	// true=closed, false=open
	boolean contact;

	@Override
	public String getTitle() {
		return "Window Contact open";
	}

	@Override
	public String getMessageString() {
		return "Window Contact open detected: " + device;
	}
}
