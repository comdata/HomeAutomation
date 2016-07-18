package cm.homeautomation.admin.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.admin.SensorAdminService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.base.GenericStatus;

public class SensorAdminServiceTest {

	private static final String TEST_ROOM = "Test Room";
	private SensorAdminService sensorAdminService;
	private EntityManager em;
	private Room room;

	@Before
	public void setup() {
		em = EntityManagerService.getNewManager();
		sensorAdminService = new SensorAdminService();
		
		em.getTransaction().begin();
		room = new Room();
		room.setRoomName(TEST_ROOM);

		em.persist(room);
		em.getTransaction().commit();
	}
	
	@After
	public void cleanup() {
		em.getTransaction().begin();

		@SuppressWarnings("unchecked")
		List<Room> rooms = (List<Room>) em.createQuery("select r from Room r where r.name=:roomName")
				.setParameter("roomName", TEST_ROOM).getResultList();

		for (Room room : rooms) {
			em.remove(room);
		}
		
		em.getTransaction().commit();
	}

	//@Test
	public void testCreateSensor() throws Exception {
		GenericStatus createSensorResult = sensorAdminService.createSensor(room.getId(), "Test Sensor", "TEMPERATURE");
		Sensor sensor=(Sensor)createSensorResult.getObject();
		
		
		assertNotNull(sensor.getId());
		assertTrue(sensor.getId().longValue()>0);
		assertTrue(createSensorResult.isSuccess());
		
		/*em.getTransaction().begin();
		sensor=em.merge(sensor);
		em.remove(sensor);
		em.getTransaction().commit();*/
	}

	//@Test
	public void testUpdateSensor() throws Exception {
		Long sensorId = new Long(1);
		GenericStatus updateSensorResult = sensorAdminService.updateSensor(sensorId);
		assertTrue(updateSensorResult.isSuccess());
	}
}
