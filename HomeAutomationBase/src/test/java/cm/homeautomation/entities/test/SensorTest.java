package cm.homeautomation.entities.test;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;

public class SensorTest {
	private EntityManager em;
	private Room room;
	

	@Before
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
		sensor.setRoom(room);
		sensor.setSensorName("Testsensor");
		
		em.persist(sensor);
		em.getTransaction().commit();

		assertTrue("Id: " + sensor.getId(), sensor.getId() != null);
	}

	@Test(expected=org.eclipse.persistence.exceptions.DatabaseException.class)
	public void testCreateEmptySensor() throws Exception {
		em.getTransaction().begin();
		Sensor sensor = new Sensor();
		sensor.setRoom(room);
		em.persist(sensor);
		em.getTransaction().commit();
		assertTrue("Id: "+sensor.getId(), sensor.getId()==null);
	}
}
