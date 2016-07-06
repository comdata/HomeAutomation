package cm.homeautomation.entities.test;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;

public class RoomTest {
	private EntityManager em;
	

	@Before
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
	}
}
