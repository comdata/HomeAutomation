package cm.homeautomation.entities;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@Entity
public class Device {

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

	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID", nullable = true)
	

	private Room room;

	@Column(name = "MAC", nullable = false, unique = true)
	private String mac;

	@Column(name = "NAME", nullable = false)
	private String name;

	@OneToMany
    private Map<String, Sensor> sensors;

}
