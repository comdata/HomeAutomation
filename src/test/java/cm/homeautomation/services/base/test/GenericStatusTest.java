package cm.homeautomation.services.base.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cm.homeautomation.services.base.GenericStatus;

public class GenericStatusTest {

	@Test
	public void testStatusEmpty() throws Exception {
		
		GenericStatus genericStatus = new GenericStatus();
		
		assertFalse(genericStatus.isSuccess());
	}
	
	@Test
	public void testStatusSuccess() throws Exception {
		
		GenericStatus genericStatus = new GenericStatus(true);
		
		assertTrue(genericStatus.isSuccess());
	}
	
	@Test
	public void testStatusFailed() throws Exception {
		
		GenericStatus genericStatus = new GenericStatus(false);
		
		assertFalse(genericStatus.isSuccess());
	}
}
