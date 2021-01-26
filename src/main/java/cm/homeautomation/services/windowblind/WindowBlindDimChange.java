package cm.homeautomation.services.windowblind;

import cm.homeautomation.services.windowblind.WindowBlindService.DimDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindowBlindDimChange {

	DimDirection direction;
	long externalId;
}
