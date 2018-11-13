package cm.homeautomation.eventbus.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.eventbus.CustomEventBus;
import cm.homeautomation.eventbus.EventBusService;

public class EventBusServiceTest {

	@BeforeEach
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

		assertTrue(eventBus.getClasses().size() == 1);

		eventBus.register(tvService);
		assertTrue(eventBus.getClasses().size() == 1);
		eventBus.unregister(tvService);
	}

	@Test
	public void testUnregister() throws Exception {
		CustomEventBus eventBus = EventBusService.getEventBus();

		EventBusServiceTestSubscriber tvService = new EventBusServiceTestSubscriber();
		eventBus.register(tvService);

		assertTrue(eventBus.getClasses().size() == 1);

		eventBus.unregister(tvService);
		assertTrue(eventBus.getClasses().size() == 0);
	}

	@Test
	public void testInit() throws Exception {

		// default test
		EventBusService.init();
		
		assertNotNull(EventBusService.getEventBus());
	}

}
