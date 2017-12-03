package cm.homeautomation.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

	@OneToMany(mappedBy = "referencedSwitch", cascade = CascadeType.ALL)
	@JsonManagedReference("referencedSwitch")
	private List<Light> lights;

	@JoinColumn(nullable = true)
	@OneToOne
	private IRCommand irCommand;

	@Column(name = "SWITCH_SET_URL")
	private String switchSetUrl;

	@Column(name = "SUBTYPE")
	private String subType;

	@OneToOne
	@JoinColumn(name = "SENSOR_ID")
	private Sensor sensor;

	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	@XmlTransient
	@JsonIgnore
	private Room room;

	@Transient
	private boolean switchState;

	public String getHouseCode() {
		return houseCode;
	}

	public Long getId() {
		return id;
	}

	public IRCommand getIrCommand() {
		return irCommand;
	}

	public String getLatestStatus() {
		return latestStatus;
	}

	public Date getLatestStatusFrom() {
		return latestStatusFrom;
	}

	@XmlIDREF
	public List<Light> getLights() {
		return lights;
	}

	public String getName() {
		return name;
	}

	public String getOffCode() {
		return offCode;
	}

	public String getOnCode() {
		return onCode;
	}

	@XmlTransient
	@JsonIgnore
	public Room getRoom() {
		return room;
	}

	@XmlTransient
	public Sensor getSensor() {
		return sensor;
	}

	public String getSubType() {
		return subType;
	}

	public String getSwitchNo() {
		return switchNo;
	}

	public String getSwitchOffCode() {
		return switchOffCode;
	}

	public String getSwitchOnCode() {
		return switchOnCode;
	}

	public String getSwitchSetUrl() {
		return switchSetUrl;
	}

	public String getSwitchType() {
		return switchType;
	}

	public String getTargetStatus() {
		return targetStatus;
	}

	public boolean isSwitchState() {
		return switchState;
	}

	public void setHouseCode(final String houseCode) {
		this.houseCode = houseCode;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setIrCommand(final IRCommand irCommand) {
		this.irCommand = irCommand;
	}

	public void setLatestStatus(final String latestStatus) {
		this.latestStatus = latestStatus;
	}

	public void setLatestStatusFrom(final Date latestStatusFrom) {
		this.latestStatusFrom = latestStatusFrom;
	}

	@XmlIDREF
	public void setLights(final List<Light> lights) {
		this.lights = lights;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOffCode(final String offCode) {
		this.offCode = offCode;
	}

	public void setOnCode(final String onCode) {
		this.onCode = onCode;
	}

	public void setRoom(final Room room) {
		this.room = room;
	}

	public void setSensor(final Sensor sensor) {
		this.sensor = sensor;
	}

	public void setSubType(final String subType) {
		this.subType = subType;
	}

	public void setSwitchNo(final String switchNo) {
		this.switchNo = switchNo;
	}

	public void setSwitchOffCode(final String switchOffCode) {
		this.switchOffCode = switchOffCode;
	}

	public void setSwitchOnCode(final String switchOnCode) {
		this.switchOnCode = switchOnCode;
	}

	public void setSwitchSetUrl(final String switchSetUrl) {
		this.switchSetUrl = switchSetUrl;
	}

	public void setSwitchState(final boolean switchState) {
		this.switchState = switchState;
	}

	public void setSwitchType(final String switchType) {
		this.switchType = switchType;
	}

	public void setTargetStatus(final String targetStatus) {
		this.targetStatus = targetStatus;
	}

}
