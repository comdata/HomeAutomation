package cm.homeautomation.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoteControlEvent {

	String name;
	String technicalId;
	boolean poweredOnState;
}
