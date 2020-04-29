package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ZigbeeLight {
	@Id
	String ieeeAddr;

	// powered on?
	boolean powerOnState = false;

	int brightness = 0;

	float x;
	float y;

	public ZigbeeLight(String ieeeAddr, boolean powerOnState, int brightness) {
		this.ieeeAddr = ieeeAddr;
		this.powerOnState = powerOnState;
		this.brightness = brightness;

	}
}
