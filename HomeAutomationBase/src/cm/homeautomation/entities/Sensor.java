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
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="SENSORNAME", nullable=false)
	private String sensorName;
	private String sensorType;
	private String sensorPin;
	private String sensorTechnicalType;
	private String sensorPosition = "LOCAL";
	@Column(name="SHOWDATA")
	private boolean showData;
	

	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	private Room room;
	
	@OneToMany(mappedBy="sensor",fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<SensorData> sensorData;

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public String getSensorPin() {
		return sensorPin;
	}

	public void setSensorPin(String sensorPin) {
		this.sensorPin = sensorPin;
	}


	public String getSensorPosition() {
		return sensorPosition;
	}

	public void setSensorPosition(String sensorPosition) {
		this.sensorPosition = sensorPosition;
	}

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("room")
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getSensorTechnicalType() {
		return sensorTechnicalType;
	}

	public void setSensorTechnicalType(String sensorTechnicalType) {
		this.sensorTechnicalType = sensorTechnicalType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isShowData() {
		return showData;
	}

	public void setShowData(boolean showData) {
		this.showData = showData;
	}

	
}
