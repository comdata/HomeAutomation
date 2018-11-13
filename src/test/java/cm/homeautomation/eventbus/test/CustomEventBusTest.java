package cm.homeautomation.eventbus.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import cm.homeautomation.eventbus.CustomEventBus;

public class CustomEventBusTest {

	private CustomEventBus customEventBus;

	public void setup() {
		customEventBus = new CustomEventBus();
	}
	
	@Test
	public void testCustomEventBus() {
		assertNotNull(customEventBus);
	}
	
	
}
