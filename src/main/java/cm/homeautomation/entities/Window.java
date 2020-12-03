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

import lombok.Getter;

import lombok.Setter;


@Entity
@Table(name = "WINDOWS")
@Getter
@Setter
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

	private String externalId;
	
}
