package cm.homeautomation.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class RemoteControlEvent {

	public enum EventType {
		REMOTE, ON_OFF
	}

	@NonNull
	String name;
	
	@NonNull
	String technicalId;

	boolean poweredOnState;

	String click;

	@NonNull
	EventType eventType;
}
