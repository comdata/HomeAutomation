package cm.homeautomation.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class DimmableColorLight extends DimmableLight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

}
