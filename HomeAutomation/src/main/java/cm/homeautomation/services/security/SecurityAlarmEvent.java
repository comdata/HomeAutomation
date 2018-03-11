package cm.homeautomation.services.security;

import cm.homeautomation.entities.SecurityZone;
import cm.homeautomation.entities.Window;
import cm.homeautomation.messages.base.HumanMessageGenerationInterface;

public class SecurityAlarmEvent implements HumanMessageGenerationInterface {

	private SecurityZone zone;
	private Window window;

	public SecurityAlarmEvent() {

	}

	public SecurityAlarmEvent(SecurityZone zone, Window window) {
		this.zone = zone;
		this.window = window;
	}

	@Override
	public String getMessageString() {
		return "Security Zone: " + zone.getName() + " violated. Window: " + window.getName();
	}

	@Override
	public String getTitle() {

		return "Security Zone Alarm";
	}

}
