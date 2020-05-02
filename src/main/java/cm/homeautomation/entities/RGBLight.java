package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

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
	private int red;
	@Column(name = "GREEN")
	private int green;
	@Column(name = "BLUE")
	private int blue;

	@Column(name = "WHITE")
	private int white;

	private Float x;
	private Float y;

	private String mqttColorTopic;
	private String mqttColorMessage;

}
