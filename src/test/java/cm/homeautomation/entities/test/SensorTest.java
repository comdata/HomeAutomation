package cm.homeautomation.entities.test;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;

public class SensorTest {
	private EntityManager em;
	private Room room;

	@BeforeEach
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		room = new Room();
		room.setRoomName("Test Sensor Room");

		em.persist(room);
		em.getTransaction().commit();
	}

	@Test
	public void testCreateSensor() throws Exception {
		em.getTransaction().begin();
		Sensor sensor = new Sensor();
	//	sensor.setRoom(room);
		sensor.setSensorName("Testsensor");
	//	sensor.setMinValue(null);
	//	sensor.setMaxValue(null);

		em.persist(sensor);
		em.getTransaction().commit();

		//assertTrue("Id: " + sensor.getId(), sensor.getId() != null);
	}

	@Test
	public void testCreateEmptySensor() {
		Assertions.assertThrows(RollbackException.class, () -> {
			em.getTransaction().begin();
			Sensor sensor = new Sensor();
		//	sensor.setRoom(room);
			em.persist(sensor);
			em.getTransaction().commit();
		});

	}
}
