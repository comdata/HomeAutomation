package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = RGBLight.class, name = "RGBLight"),
		@JsonSubTypes.Type(value = DimmableColorLight.class, name = "DimmableColorLight") })
@Entity
public class DimmableLight extends Light {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "BRIGHTNESS")
	private int brightnessLevel = 0;

	@Column(name = "MINIMUM_VALUE")
	private int minimumValue = 0;

	@Column(name = "MAXIMUM_VALUE")
	private int maximumValue = 0;

	public int getBrightnessLevel() {
		return brightnessLevel;
	}

	public int getMaximumValue() {
		return maximumValue;
	}

	public int getMinimumValue() {
		return minimumValue;
	}

	public void setBrightnessLevel(final int brightnessLevel) {
		this.brightnessLevel = brightnessLevel;
	}

	public void setMaximumValue(final int maximumValue) {
		this.maximumValue = maximumValue;
	}

	public void setMinimumValue(final int minimumValue) {
		this.minimumValue = minimumValue;
	}

}
