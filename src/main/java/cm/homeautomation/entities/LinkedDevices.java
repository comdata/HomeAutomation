package cm.homeautomation.entities;

import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LinkedDevices {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@ElementCollection
	Map<Long, String> linkedDevices;

}
