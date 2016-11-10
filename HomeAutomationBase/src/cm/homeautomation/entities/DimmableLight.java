package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DimmableLight extends Light {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="BRIGHTNESS")
	private int brightnessLevel;

	public int getBrightnessLevel() {
		return brightnessLevel;
	}

	public void setBrightnessLevel(int brightnessLevel) {
		this.brightnessLevel = brightnessLevel;
	}

}
