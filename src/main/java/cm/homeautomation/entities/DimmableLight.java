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
public class DimmableLight extends Light {

    @Getter
    @Setter    
	@Column(name = "BRIGHTNESS")
	private int brightnessLevel = 0;

    @Getter
    @Setter
	@Column(name = "MINIMUM_VALUE")
	private int minimumValue = 0;

    @Getter
    @Setter
	@Column(name = "MAXIMUM_VALUE")
	private int maximumValue = 0;

}
