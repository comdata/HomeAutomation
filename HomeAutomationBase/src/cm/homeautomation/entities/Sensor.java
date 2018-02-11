package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "SENSORNAME", nullable = false)
	private String sensorName;
	private String sensorType;
	private String sensorPin;
	private String sensorTechnicalType;
	private String sensorPosition = "LOCAL";
	private int deadbandPercent = 0;

	@Column(name = "SHOWDATA")
	private boolean showData;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	private Room room;

	@OneToMany(mappedBy = "sensor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SensorData> sensorData;

	public int getDeadbandPercent() {
		return deadbandPercent;
	}

	public Long getId() {
		return id;
	}

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("room")
	public Room getRoom() {
		return room;
	}

	public String getSensorName() {
		return sensorName;
	}

	public String getSensorPin() {
		return sensorPin;
	}

	public String getSensorPosition() {
		return sensorPosition;
	}

	public String getSensorTechnicalType() {
		return sensorTechnicalType;
	}

	public String getSensorType() {
		return sensorType;
	}

	public boolean isShowData() {
		return showData;
	}

	public void setDeadbandPercent(int deadbandPercent) {
		this.deadbandPercent = deadbandPercent;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public void setSensorPin(String sensorPin) {
		this.sensorPin = sensorPin;
	}

	public void setSensorPosition(String sensorPosition) {
		this.sensorPosition = sensorPosition;
	}

	public void setSensorTechnicalType(String sensorTechnicalType) {
		this.sensorTechnicalType = sensorTechnicalType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public void setShowData(boolean showData) {
		this.showData = showData;
	}

}
