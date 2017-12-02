package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class DimmableColorLight extends DimmableLight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "COLOR_VALUE")
	private String colorUrl;

	private String color;

	public String getColor() {
		return color;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public String getColorUrl() {
		return colorUrl;
	}

	public void setColorUrl(String colorUrl) {
		this.colorUrl = colorUrl;
	}
}
