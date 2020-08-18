package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RGBLight extends DimmableColorLight {

	@Column(name = "RED")
	private int red = 0;
	@Column(name = "GREEN")
	private int green = 0;
	@Column(name = "BLUE")
	private int blue = 0;

	@Column(name = "WHITE")
	private int white = 0;

	@Enumerated(EnumType.STRING)
	private RGBLightType rgbLightType = RGBLightType.RGB;

	private Float x = 0F;
	private Float y = 0F;

	private String mqttColorTopic;
	private String mqttColorMessage;

}
