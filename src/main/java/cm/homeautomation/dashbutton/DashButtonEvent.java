package cm.homeautomation.dashbutton;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashButtonEvent {

	String mac;
	short secs;
	
	public DashButtonEvent(String mac, short secs) {
		this.mac=mac;
		this.secs=secs;
	}
}
