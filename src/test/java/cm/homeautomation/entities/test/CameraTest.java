package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.entities.Room;

public class CameraTest {

	private static final String TEST_CAMERA_ROOM = "Test Camera Room";
	private EntityManager em;
	private Room room;

	@BeforeEach
	public void setup() {

		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		em.createQuery("delete from Camera").executeUpdate();
		em.createQuery("delete from Room r where r.roomName=:roomName").setParameter("roomName", TEST_CAMERA_ROOM)
				.executeUpdate();

		room = new Room();
		room.setRoomName(TEST_CAMERA_ROOM);

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

		assertTrue(camera.getId() != null, "Id: " + camera.getId());
	}

	@Test
	public void testCameraFailsWithoutRoom() throws Exception {
		Assertions.assertThrows(RollbackException.class, () -> {
			em = EntityManagerService.getNewManager();
			em.getTransaction().begin();
			Camera camera = new Camera();

			camera.setCameraName("Testblind");
			em.persist(camera);
			em.getTransaction().commit();
		});
	}
}
