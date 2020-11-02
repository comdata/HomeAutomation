package cm.homeautomation.services.windowblind;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class WindowBlindPositionEvent {
	private Long windowBlindId;
	private String position;
}
