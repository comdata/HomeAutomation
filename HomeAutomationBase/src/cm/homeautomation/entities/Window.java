package cm.homeautomation.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "WINDOWS")
public class Window {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@JsonIdentityReference(alwaysAsId = true)
	@JsonBackReference("room")
	@ManyToOne
	@JoinColumn(name = "ROOM_ID")
	private Room room;

	@OneToOne
	@JoinColumn(nullable = true)
	private Sensor stateSensor;

	@Column(name = "NAME")
	private String name;

	@Column(nullable = true)
	private String mac;

	public Long getId() {
		return id;
	}

	public String getMac() {
		return mac;
	}

	public String getName() {
		return name;
	}

	public Room getRoom() {
		return room;
	}

	public Sensor getStateSensor() {
		return stateSensor;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public void setStateSensor(Sensor stateSensor) {
		this.stateSensor = stateSensor;
	}

}
