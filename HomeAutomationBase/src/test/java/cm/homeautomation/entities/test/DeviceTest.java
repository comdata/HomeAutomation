package cm.homeautomation.entities.test;

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
		
		room=new Room();
		room.setRoomName("Test Device Room");

		em.persist(room);
	}
	
	@Test
	public void testCreateDevice() throws Exception {
		
		Device device = new Device();
		
		device.setMac("00:00:00:00:00");
		device.setName("Test device");
		device.setRoom(room);
		
		em.persist(device );
	}
}
