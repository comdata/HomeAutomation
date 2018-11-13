package cm.homeautomation.dashbutton.test;

import javax.annotation.Generated;

import org.junit.Test;

import cm.homeautomation.dashbutton.DashButtonService;

@Generated(value = "org.junit-tools-1.1.0")
public class DashButtonServiceTest {

	private DashButtonService createTestSubject() {
		return new DashButtonService();
	}

	@Test
	public void testRun() throws Exception {
		DashButtonService testSubject = createTestSubject();

		testSubject.run();
	}

}