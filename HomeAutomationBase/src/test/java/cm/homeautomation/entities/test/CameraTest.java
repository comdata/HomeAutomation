package cm.homeautomation.entities.test;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;

public class CameraTest {

	private EntityManager em;
	private Room room;

	@Before
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		room = new Room();
		room.setRoomName("Test Camera Room");

		em.persist(room);
		em.getTransaction().commit();
	}

	@Test
	public void testCreateCamera() throws Exception {
		em.getTransaction().begin();
		Camera camera = new Camera();

		camera.setIcon("Test Icon");
		camera.setCameraName("Test device");
		camera.setRoom(room);

		em.persist(camera);

		em.getTransaction().commit();
		
		assertTrue("Id: " + camera.getId(), camera.getId() != null);
	}
	
	@Test(expected=RollbackException.class)
	public void testCameraFailsWithoutRoom() throws Exception {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		Camera camera = new Camera();

		camera.setCameraName("Testblind");
		em.persist(camera);
		em.getTransaction().commit();		
	}
}
