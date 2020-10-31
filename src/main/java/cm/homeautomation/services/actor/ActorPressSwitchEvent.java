package cm.homeautomation.services.actor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ActorPressSwitchEvent {
	String switchId;
	String targetStatus;
}
