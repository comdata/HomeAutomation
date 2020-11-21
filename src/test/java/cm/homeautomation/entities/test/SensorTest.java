package cm.homeautomation.entities.test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;

public class SensorTest {
	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;
	private Room room;

	@BeforeEach
	public void setup() {
		
		

		room = new Room();
		room.setRoomName("Test Sensor Room");

		em.persist(room);
		
	}

	@Test
	public void testCreateSensor() throws Exception {
		
		Sensor sensor = new Sensor();
	//	sensor.setRoom(room);
		sensor.setSensorName("Testsensor");
	//	sensor.setMinValue(null);
	//	sensor.setMaxValue(null);

		em.persist(sensor);
		

		//assertTrue("Id: " + sensor.getId(), sensor.getId() != null);
	}

	@Test
	public void testCreateEmptySensor() {
		Assertions.assertThrows(RollbackException.class, () -> {
			
			Sensor sensor = new Sensor();
		//	sensor.setRoom(room);
			em.persist(sensor);
			
		});

	}
}
