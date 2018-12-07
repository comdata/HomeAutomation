package cm.homeautomation.services.led;

import org.junit.Test;

public class LEDServiceTest {

	@Test
	public void testSetLed() throws Exception {
		LEDService ledService = new LEDService();
		ledService.setLed(255, 255, 255);
	}

}
