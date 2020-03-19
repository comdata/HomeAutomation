
package cm.homeautomation.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlIDREF;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="ROOMNAME", nullable=false)
	private String roomName;
	
	@OneToMany(mappedBy="room", cascade=CascadeType.ALL)
	@JsonManagedReference("room")
	@XmlIDREF
	private List<Sensor> sensors;

	@OneToMany(mappedBy="room", cascade=CascadeType.ALL)
	@JsonManagedReference("room")
	
	@XmlIDREF
	private List<Switch> switches;
	
	@OneToMany(mappedBy="room", cascade=CascadeType.ALL)
	@JsonManagedReference("room")

	@XmlIDREF
	private List<Device> devices;

	@OneToMany(mappedBy="room", cascade=CascadeType.ALL)
	@JsonManagedReference("room")

	@XmlIDREF
	private List<Light> lights;
	
	@Column(name="VISIBLE")
	private Boolean visible;
	
	@Column(name="SORT_ORDER")
	private int sortOrder=0;
	
	@OneToMany(mappedBy="room", cascade=CascadeType.ALL)
	@JsonManagedReference("room")

	@XmlIDREF
	private List<RoomProperty> roomProperty;
	
	

}
