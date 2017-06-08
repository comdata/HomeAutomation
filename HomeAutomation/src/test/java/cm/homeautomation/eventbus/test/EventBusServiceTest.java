package cm.homeautomation.eventbus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cm.homeautomation.eventbus.CustomEventBus;
import cm.homeautomation.eventbus.EventBusService;

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
}
