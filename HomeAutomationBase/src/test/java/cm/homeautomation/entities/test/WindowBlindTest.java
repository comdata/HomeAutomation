package cm.homeautomation.entities.test;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;

public class WindowBlindTest {
	private EntityManager em;
	private String calibrationUrl="http://www.google.de";
	private WindowBlind windowBlind;
	private Room room;
	
	@Before
	public void setup() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		room = new Room();
		room.setRoomName("Test Sensor Room");

		em.persist(room);
	}
	
	@After
	public void tearDown() {
		em.getTransaction().commit();
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		windowBlind=em.merge(windowBlind);
		em.remove(windowBlind);
		em.getTransaction().commit();
	}
	
	@Test
	public void testCreateBlind() throws Exception {
		windowBlind = new WindowBlind();
		
		windowBlind.setName("Testblind");
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		em.getTransaction().commit();
	}
	
	@Test(expected=PersistenceException.class)
	public void testWindowBlindFailsWithoutRoom() throws Exception {
		windowBlind = new WindowBlind();
		
		windowBlind.setName("Testblind");
		em.persist(windowBlind);
		em.getTransaction().commit();		
	}
	
	@Test
	public void testCalibrationURL() throws Exception {
		
		
		windowBlind = new WindowBlind();
		
		windowBlind.setCalibrationUrl(calibrationUrl);
		windowBlind.setRoom(room);
		em.persist(windowBlind);
		em.getTransaction().commit();
		
		assertTrue("id is null",windowBlind.getId()!=null);
		assertTrue("id is not greater 0", windowBlind.getId().longValue()>0);
		assertTrue("calibration Url is not: " + calibrationUrl, calibrationUrl.equals(windowBlind.getCalibrationUrl()));
	}
}
