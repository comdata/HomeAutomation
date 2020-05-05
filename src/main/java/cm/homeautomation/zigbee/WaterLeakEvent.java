package cm.homeautomation.zigbee;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class WaterLeakEvent implements HumanMessageGenerationInterface {

	String device;

	@Override
	public String getTitle() {
		return "Water Leak";
	}

	@Override
	public String getMessageString() {
		return "Water Leak detected: " + device;
	}

}
