package cm.homeautomation.services.networkmonitor;

import cm.homeautomation.messages.base.HumanMessageGenerationInterface;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class NetworkWakeupEvent implements HumanMessageGenerationInterface{

    @NonNull
	private String mac;

	@Override
	public String getTitle() {

		return "Network Wakeup";
	}

	@Override
	public String getMessageString() {
		
		return "Device with MAC: "+getMac()+" woken up.";
	}

}
