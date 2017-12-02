package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RGBLight extends DimmableColorLight {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "RED")
	private int red;
	@Column(name = "GREEN")
	private int green;
	@Column(name = "BLUE")
	private int blue;

	@Column(name = "WHITE")
	private int white;

	public int getBlue() {
		return blue;
	}

	public int getGreen() {
		return green;
	}

	public int getRed() {
		return red;
	}

	public int getWhite() {
		return white;
	}

	public void setBlue(final int blue) {
		this.blue = blue;
	}

	public void setGreen(final int green) {
		this.green = green;
	}

	public void setRed(final int red) {
		this.red = red;
	}

	public void setWhite(final int white) {
		this.white = white;
	}
}
