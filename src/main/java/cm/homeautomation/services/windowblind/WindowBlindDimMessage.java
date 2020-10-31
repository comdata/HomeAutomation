package cm.homeautomation.services.windowblind;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WindowBlindDimMessage {
	Long windowBlindId;
	String value;
	String type;
	Long roomId;
}
