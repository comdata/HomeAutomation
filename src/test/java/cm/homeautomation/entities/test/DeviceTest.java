package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class DeviceTest {

	private static final String TEST_DEVICE_ROOM = "Test Device Room";
	private EntityManager em;
	private Room room;

	@BeforeEach
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		em.createQuery("delete from Device").executeUpdate();
		em.createQuery("delete from Room r where r.roomName=:roomName").setParameter("roomName", TEST_DEVICE_ROOM)
				.executeUpdate();
		
		room = new Room();
		room.setRoomName(TEST_DEVICE_ROOM);

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
		
		assertTrue(device.getId() != null, "Id: " + device.getId());
	}
}
