package cm.homeautomation.services.windowblind;

import javax.ws.rs.PathParam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WindowBlindDimMessageSimple {
	Long windowBlindId;
	String value;
}
