package cm.homeautomation.fhem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FHEMDataEvent {
	String topic;
	String payload;
}
