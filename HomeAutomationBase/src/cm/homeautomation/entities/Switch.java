package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class Switch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	private String onCode;
	private String offCode;
	private String latestStatus;
	private Date latestStatusFrom;
	private String targetStatus;
	private String houseCode;
	private String switchNo;
	private String switchOnCode;
	private String switchOffCode;
	private String switchType;
	
	@Column(name="SWITCH_SET_URL")
	private String switchSetUrl;
	
	@Column(name="SUBTYPE")
	private String subType;
	
	@OneToOne
	@JoinColumn(name="SENSOR_ID")
	private Sensor sensor;
	
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	private Room room;
	
	@Transient
	private boolean switchState;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOnCode() {
		return onCode;
	}

	public void setOnCode(String onCode) {
		this.onCode = onCode;
	}

	public String getOffCode() {
		return offCode;
	}

	public void setOffCode(String offCode) {
		this.offCode = offCode;
	}

	public String getLatestStatus() {
		return latestStatus;
	}

	public void setLatestStatus(String latestStatus) {
		this.latestStatus = latestStatus;
	}

	public Date getLatestStatusFrom() {
		return latestStatusFrom;
	}

	public void setLatestStatusFrom(Date latestStatusFrom) {
		this.latestStatusFrom = latestStatusFrom;
	}

	public String getTargetStatus() {
		return targetStatus;
	}

	public void setTargetStatus(String targetStatus) {
		this.targetStatus = targetStatus;
	}

	public String getHouseCode() {
		return houseCode;
	}

	public void setHouseCode(String houseCode) {
		this.houseCode = houseCode;
	}

	public String getSwitchNo() {
		return switchNo;
	}

	public void setSwitchNo(String switchNo) {
		this.switchNo = switchNo;
	}

	public String getSwitchOnCode() {
		return switchOnCode;
	}

	public void setSwitchOnCode(String switchOnCode) {
		this.switchOnCode = switchOnCode;
	}

	public String getSwitchOffCode() {
		return switchOffCode;
	}

	public void setSwitchOffCode(String switchOffCode) {
		this.switchOffCode = switchOffCode;
	}

	@XmlTransient
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getSwitchType() {
		return switchType;
	}

	public void setSwitchType(String switchType) {
		this.switchType = switchType;
	}

	public boolean isSwitchState() {
		return switchState;
	}

	public void setSwitchState(boolean switchState) {
		this.switchState = switchState;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlTransient
	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public String getSwitchSetUrl() {
		return switchSetUrl;
	}

	public void setSwitchSetUrl(String switchSetUrl) {
		this.switchSetUrl = switchSetUrl;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

}
