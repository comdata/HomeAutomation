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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = MQTTSwitch.class, name = "MQTTSwitch"), })
@Entity
@Getter
@Setter

public class Switch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Dates
	private Date dateInstalled;

	private Date dateLastSeen;
	private String manufacturer;

	private String type;
	private String firmware;
	private String externalId;

	private String name;
	private String onCode;
	private String offCode;
	private String latestStatus;
	private Date latestStatusFrom;
	private String targetStatus;
	private Date targetStatusFrom;
	private String houseCode;
	private String switchNo;
	private String switchOnCode;
	private String switchOffCode;
	private String switchType;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@OneToMany(mappedBy = "referencedSwitch", cascade = CascadeType.ALL)
	@JsonManagedReference("referencedSwitch")
	private List<Light> lights;

	@JoinColumn(nullable = true, referencedColumnName = "ID")
	@OneToOne(optional = true)

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

	@Column(name = "VISIBLE", nullable = false)
	private boolean visible = true;

	@Transient
	private boolean switchState;

	@XmlIDREF
	public List<Light> getLights() {
		return lights;
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

	@XmlIDREF
	public void setLights(final List<Light> lights) {
		this.lights = lights;
	}

}
