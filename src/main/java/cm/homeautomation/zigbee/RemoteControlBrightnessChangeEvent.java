package cm.homeautomation.zigbee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoteControlBrightnessChangeEvent {
	String name;
	String technicalId;
	boolean poweredOnState;

	int brightness;

}
