package cm.homeautomation.services.phone.test;

import static org.junit.jupiter.Assert.*;

import org.junit.jupiter.Before;
import org.junit.jupiter.Test;

import java.util.List;

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
	}
	
	@Test
	public void testPhoneCall() throws Exception {
		
		phone.setStatus("ring", "incoming", "123", "456");
		
		List<PhoneCallEvent> callList = phone.getCallList();
		
		assertTrue(callList.size()==1);
		assertTrue("456".equals(callList.get(0).getExternalNumber()));
	}

}
