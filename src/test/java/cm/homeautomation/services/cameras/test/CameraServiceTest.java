package cm.homeautomation.services.cameras.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.validation.constraints.AssertTrue;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.cameras.CameraService;

public class CameraServiceTest {
	private static final String CAMERA_ROOM = "camera room";
	private Camera camera;
	private EntityManager em;
	private Room room;
	private CameraService cameraService;

	@Before
	public void setup() {
		camera = new Camera();
		em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		em.createQuery("delete from Camera").executeUpdate();
		em.createQuery("delete from Room r where r.roomName=:roomName").setParameter("roomName", CAMERA_ROOM)
				.executeUpdate();
		
		room = new Room();
		room.setRoomName(CAMERA_ROOM);
		
		em.persist(room);
		
		camera.setCameraName("Test Room");
		camera.setRoom(room);
		
		em.getTransaction().commit();
	}
	
	@Test
	public void testReadAllService() throws Exception {
		
		em.getTransaction().begin();
		
		em.persist(camera);
		
		em.getTransaction().commit();
		
		cameraService = new CameraService();
		
		List<Camera> cameraList = cameraService.getAll();
		
		assertNotNull("Camera list is empty", cameraList);
		assertTrue("camera list set", cameraList.isEmpty()==false);
	}
}
