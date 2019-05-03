package cm.homeautomation.eventbus.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.eventbus.CustomEventBus;

public class CustomEventBusTest {

	private CustomEventBus customEventBus;

	@BeforeEach
	public void setup() {
		customEventBus = new CustomEventBus();
	}
	
	@Test
	public void testCustomEventBus() {
		assertNotNull(customEventBus);
	}
	
	@Test
	public void testGetEventBus() {
		EventBus eventBus = CustomEventBus.getEventBus();
		assertNotNull(eventBus);
	}
	
}
