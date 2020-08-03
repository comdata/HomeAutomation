package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;

public class RoomTest {
	private EntityManager em;
	

	@BeforeEach
	public void setup() {
		em = EntityManagerService.getNewManager();
		

	}
	
	@Test
	public void testCreateRoom() throws Exception {
		em.getTransaction().begin();
		Room room=new Room();
		room.setRoomName("Test Room");

		em.persist(room);
		em.getTransaction().commit();
		
		assertNotNull(room);
	}
}
