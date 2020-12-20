package cm.homeautomation.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

	private String lightGroup;

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

	@Column(name = "COLOR")
	private String color;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "LIGHTSUPPORTEDTEMPERATURES", joinColumns=@JoinColumn(name="LIGHTTEMP_ID") )
	@Column(name="lightTempValues")
	private List<Integer> supportedlightTemperatures;

	private String mqttPowerOnTopic;
	private String mqttPowerOffTopic;
	private String mqttPowerOnMessage;
	private String mqttPowerOffMessage;
	private String mqttLightTemperatureTopic;
	private String mqttLightTemperatureMessage;

	// Status
	private boolean online;


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

}
