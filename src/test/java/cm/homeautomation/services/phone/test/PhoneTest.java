package cm.homeautomation.services.phone.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.services.phone.Phone;

/**
 * Phone Call recording and event handling tests
 * 
 * @author christoph
 *
 */
public class PhoneTest {
	
	private Phone phone;

	@Before
	public void setup() {
		phone = new Phone();
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		em.createQuery("delete from PhoneCallEvent").executeUpdate();
		em.getTransaction().commit();

	}
	
	@Test
	public void testPhoneCall() throws Exception {
		
		phone.setStatus("ring", "incoming", "123", "456");
		
		List<PhoneCallEvent> callList = phone.getCallList();
		
		assertTrue(callList.size()==1);
		assertTrue("456".equals(callList.get(0).getExternalNumber()));
	}

}
