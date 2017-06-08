package cm.homeautomation.eventbus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cm.homeautomation.eventbus.CustomEventBus;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.services.tv.TVService;

public class EventBusServiceTest {

	
	@Test
	public void testEventBusService() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();
		
		assertNotNull(eventBus);
	}
	
	
	@Test
	public void testMultiCallEventBusService() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();
		CustomEventBus eventBus2 = EventBusService.getEventBus();
		
		assertEquals(eventBus, eventBus2);
	}
	
	@Test
	public void testAlreadyRegistered() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();
		
		TVService tvService = new TVService();
		eventBus.register(tvService);
		
		assertTrue(eventBus.getClasses().size()==1);
		
		eventBus.register(tvService);
		assertTrue(eventBus.getClasses().size()==1);
	}
	
	@Test
	public void testUnregister() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();
		
		TVService tvService = new TVService();
		eventBus.register(tvService);
		
		assertTrue(eventBus.getClasses().size()==1);
		
		eventBus.unregister(tvService);
		assertTrue(eventBus.getClasses().size()==0);
	}
	
	
}
