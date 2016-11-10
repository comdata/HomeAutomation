package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RGBLight extends DimmableLight {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="RED")
	private int red;
	@Column(name="GREEN")
	private int green;
	@Column(name="BLUE")
	private int blue;
	
	@Column(name="WHITE")
	private int white;
	
	public int getBlue() {
		return blue;
	}
	public void setBlue(int blue) {
		this.blue = blue;
	}
	public int getGreen() {
		return green;
	}
	public void setGreen(int green) {
		this.green = green;
	}
	public int getRed() {
		return red;
	}
	public void setRed(int red) {
		this.red = red;
	}
	public int getWhite() {
		return white;
	}
	public void setWhite(int white) {
		this.white = white;
	}
}
