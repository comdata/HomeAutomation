package cm.homeautomation.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = DimmableLight.class, name = "DimmableLight"),
		@JsonSubTypes.Type(value = DimmableColorLight.class, name = "DimmableColorLight") })
@Entity
public class Light {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String lightType;

	@Column(name = "POWERSTATE")
	private boolean powerState;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	private Room room;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("referencedSwitch")
	@ManyToOne
	@JoinColumn(name = "SWITCH_ID")
	private Switch referencedSwitch;

	@JsonIgnore
	private String dimUrl;

	private String name;

	// Dates
	private Date dateInstalled;

	private Date dateLastSeen;
	private String manufacturer;

	private String type;
	private String firmware;
	private String externalId;

	@Column(name = "COLOR_VALUE")
	private String colorUrl;

	// Status
	private boolean online;

	public String getColorUrl() {
		return colorUrl;
	}

	public Date getDateInstalled() {
		return dateInstalled;
	}

	public Date getDateLastSeen() {
		return dateLastSeen;
	}

	@JsonIgnore
	public String getDimUrl() {
		return dimUrl;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getFirmware() {
		return firmware;
	}

	public Long getId() {
		return id;
	}

	public String getLightType() {
		return lightType;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getName() {
		return name;
	}

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("referencedSwitch")
	public Switch getReferencedSwitch() {
		return referencedSwitch;
	}

	@XmlTransient
	@JsonIgnore
	@JsonBackReference("room")
	public Room getRoom() {
		return room;
	}

	public String getType() {
		return type;
	}

	public boolean isOnline() {
		return online;
	}

	public boolean isPowerState() {
		return powerState;
	}

	public void setColorUrl(final String colorUrl) {
		this.colorUrl = colorUrl;
	}

	public void setDateInstalled(final Date dateInstalled) {
		this.dateInstalled = dateInstalled;
	}

	public void setDateLastSeen(final Date dateLastSeen) {
		this.dateLastSeen = dateLastSeen;
	}

	public void setDimUrl(final String dimUrl) {
		this.dimUrl = dimUrl;
	}

	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	public void setFirmware(final String firmware) {
		this.firmware = firmware;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setLightType(final String lightType) {
		this.lightType = lightType;
	}

	public void setManufacturer(final String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOnline(final boolean online) {
		this.online = online;
	}

	public void setPowerState(final boolean powerState) {
		this.powerState = powerState;
	}

	public void setReferencedSwitch(final Switch referencedSwitch) {
		this.referencedSwitch = referencedSwitch;
	}

	public void setRoom(final Room room) {
		this.room = room;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
