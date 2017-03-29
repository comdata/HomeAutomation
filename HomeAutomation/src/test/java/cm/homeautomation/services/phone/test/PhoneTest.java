package cm.homeautomation.services.phone.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.services.phone.Phone;

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
