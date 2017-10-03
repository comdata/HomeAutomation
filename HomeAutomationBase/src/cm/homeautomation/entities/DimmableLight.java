package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = RGBLight.class, name = "RGBLight") })
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

	public void setBrightnessLevel(int brightnessLevel) {
		this.brightnessLevel = brightnessLevel;
	}

	public int getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(int minimumValue) {
		this.minimumValue = minimumValue;
	}

	public int getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(int maximumValue) {
		this.maximumValue = maximumValue;
	}

}
