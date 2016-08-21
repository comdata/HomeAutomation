package cm.homeautomation.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
public class WindowBlind {
	public static final String ALL_AT_ONCE = "ALL_AT_ONCE";

	public static final String SINGLE = "SINGLE";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@ManyToOne
	@JoinColumn(name = "ROOM_ID", nullable = false)
	private Room room;

	private String name;

	private String statusUrl;
	private String dimUrl;
	private String calibrationUrl;
	private float currentValue;

	@Transient
	private String windowBlindType = WindowBlind.SINGLE;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatusUrl() {
		return statusUrl;
	}

	public void setStatusUrl(String statusUrl) {
		this.statusUrl = statusUrl;
	}

	public String getDimUrl() {
		return dimUrl;
	}

	public void setDimUrl(String dimUrl) {
		this.dimUrl = dimUrl;
	}

	public float getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(float currentValue) {
		this.currentValue = currentValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setType(String windowBlindType) {
		this.windowBlindType = windowBlindType;

	}

	public String getType() {
		return windowBlindType;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getCalibrationUrl() {
		return calibrationUrl;
	}

	public void setCalibrationUrl(String calibrationUrl) {
		this.calibrationUrl = calibrationUrl;
	}
}
