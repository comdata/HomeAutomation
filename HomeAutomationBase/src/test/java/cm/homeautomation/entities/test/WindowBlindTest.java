package cm.homeautomation.entities.test;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.WindowBlind;

public class WindowBlindTest {
	private EntityManager em;
	private String calibrationUrl="http://www.google.de";
	private WindowBlind windowBlind;
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void tearDown() {
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();		
		em.remove(windowBlind);
		em.getTransaction().commit();
	}
	
	@Test
	public void testCalibrationURL() throws Exception {
		
		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		windowBlind = new WindowBlind();
		
		windowBlind.setCalibrationUrl(calibrationUrl);
		
		em.getTransaction().commit();
		
		assertTrue(windowBlind.getId()!=null);
		assertTrue(windowBlind.getId().longValue()>0);
		assertTrue(calibrationUrl.equals(windowBlind.getCalibrationUrl()));
	}
}
