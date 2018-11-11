package cm.homeautomation.windowblind.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.windowblind.WindowBlindService;
import cm.homeautomation.windowblind.WindowBlindsList;

public class WindowBlindServiceTest {

	private WindowBlindService windowBlindService;
	private WindowBlind singleWindowBlindOne;
	private WindowBlind windowBlind;
	private Room roomOne;
	private Room roomTwo;
	private WindowBlind singleWindowBlindTwo;
	private EntityManager em;

	@BeforeEach
	public void setup() {
		windowBlindService = new WindowBlindService();

		em = EntityManagerService.getNewManager();

		em.getTransaction().begin();
		// clear all
		deleteExistingEntries(em);

		roomOne = new Room();
		roomOne.setRoomName("WindowBlind " + System.currentTimeMillis());
		
		em.persist(roomOne);

		roomTwo = new Room();
		roomTwo.setRoomName("WindowBlind " + System.currentTimeMillis());
		
		em.persist(roomTwo);

		
		singleWindowBlindOne = new WindowBlind();

		singleWindowBlindOne.setName("Single Blind One");
		singleWindowBlindOne.setRoom(roomOne);
		singleWindowBlindOne.setType("SINGLE");

		em.persist(singleWindowBlindOne);
		
		singleWindowBlindTwo = new WindowBlind();

		singleWindowBlindTwo.setName("Single Blind Two");
		singleWindowBlindTwo.setRoom(roomTwo);
		singleWindowBlindTwo.setType("SINGLE");

		em.persist(singleWindowBlindTwo);
		
		em.getTransaction().commit();
	}

	private void deleteExistingEntries(EntityManager em) {
		em.createQuery("delete from WindowBlind w").executeUpdate();
		em.createQuery("delete from Room r where r.roomName like 'WindowBlind%'").executeUpdate();
	}

	@Test
	public void testGetAll() {
		WindowBlindsList all = windowBlindService.getAll();
		
		assertNotNull(all);
		assertNotNull(all.getWindowBlinds());
		assertFalse(all.getWindowBlinds().isEmpty());
		assertTrue(all.getWindowBlinds().size()==2);
	}
	
	@Test
	public void testGetAllNoWindowBlindAvailable() {
		em.getTransaction().begin();
		deleteExistingEntries(em);
		em.getTransaction().commit();
		
		WindowBlindsList all = windowBlindService.getAll();
		
		assertNotNull(all);
		assertNotNull(all.getWindowBlinds());
		assertTrue(all.getWindowBlinds().isEmpty());
	}
	
	@Test
	public void testGetAllForRoom() {
		WindowBlindsList allForRoom = windowBlindService.getAllForRoom(roomTwo.getId());
		
		assertNotNull(allForRoom);
		assertNotNull(allForRoom.getWindowBlinds());
		assertFalse(allForRoom.getWindowBlinds().isEmpty());
		assertTrue(allForRoom.getWindowBlinds().size()==2);
		assertTrue(allForRoom.getWindowBlinds().get(1).getType().equals(WindowBlind.ALL_AT_ONCE));
	}
	@Test
	public void testGetAllForRoomNoWindowBlindAvailable() {
		em.getTransaction().begin();
		deleteExistingEntries(em);
		em.getTransaction().commit();
		
		WindowBlindsList allForRoom = windowBlindService.getAllForRoom(roomTwo.getId());
		
		assertNotNull(allForRoom);
		assertNotNull(allForRoom.getWindowBlinds());
		assertTrue(allForRoom.getWindowBlinds().isEmpty());
	}
}
