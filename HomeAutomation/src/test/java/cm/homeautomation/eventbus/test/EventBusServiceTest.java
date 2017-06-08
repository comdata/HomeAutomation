package cm.homeautomation.eventbus.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.eventbus.CustomEventBus;
import cm.homeautomation.eventbus.EventBusService;

public class EventBusServiceTest {

	
	@Before
	public void setup() {
		EventBusService.init();
	}

	
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
		
		EventBusServiceTestSubscriber tvService = new EventBusServiceTestSubscriber();
		eventBus.register(tvService);
		
		assertTrue(eventBus.getClasses().size()==1);
		
		eventBus.register(tvService);
		assertTrue(eventBus.getClasses().size()==1);
		eventBus.unregister(tvService);
	}
	
	@Test
	public void testUnregister() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();
		
		EventBusServiceTestSubscriber tvService = new EventBusServiceTestSubscriber();
		eventBus.register(tvService);
		
		assertTrue(eventBus.getClasses().size()==1);
		
		eventBus.unregister(tvService);
		assertTrue(eventBus.getClasses().size()==0);
	}
	
	
}
