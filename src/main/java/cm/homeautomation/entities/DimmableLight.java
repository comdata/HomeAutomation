package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = RGBLight.class, name = "RGBLight"),
		@JsonSubTypes.Type(value = DimmableColorLight.class, name = "DimmableColorLight") })
@Entity
@Getter
@Setter
public class DimmableLight extends Light {
	@Column(name = "BRIGHTNESS")
	private int brightnessLevel = 0;

	@Column(name = "MINIMUM_VALUE")
	private int minimumValue = 0;

	@Column(name = "MAXIMUM_VALUE")
	private int maximumValue = 0;
}
