package cm.homeautomation.entities.test;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class DeviceTest {

	private EntityManager em;
	private Room room;

	@Before
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		room = new Room();
		room.setRoomName("Test Device Room");

		em.persist(room);
		em.getTransaction().commit();
	}

	@Test
	public void testCreateDevice() throws Exception {
		em.getTransaction().begin();
		Device device = new Device();

		device.setMac("00:00:00:00:00");
		device.setName("Test device");
		device.setRoom(room);

		em.persist(device);

		em.getTransaction().commit();
		
		assertTrue("Id: " + device.getId(), device.getId() != null);
	}
}
