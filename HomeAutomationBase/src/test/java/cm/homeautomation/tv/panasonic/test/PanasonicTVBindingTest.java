package cm.homeautomation.tv.panasonic.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.tv.panasonic.PanasonicTVBinding;
import cm.homeautomation.tv.panasonic.test.TVServer;

public class PanasonicTVBindingTest {

	private final int tvPort = 55000;
	private final String tvIp = "127.0.0.1";
	private TVServer tvServer;

	@Before
	public void setup() {

	}

	@After
	public void tearDown() {
		if (tvServer != null) {
			tvServer.setRun(false);
			tvServer.interrupt();
			System.out.println("Server stopped");
		}
	}

	@Test
	public void testCheckAlive() throws Exception {
		tvServer = new TVServer(tvPort);
		tvServer.setRun(true);
		tvServer.start();

		System.out.println("Running test for checkAlive");
		tvServer.setStatusCode("HTTP/1.1 200 OK");
		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean checkAlive = panasonicTVBinding.checkAlive(tvIp);

		assertTrue(checkAlive);
	}

	@Test
	public void testCheckAliveFalse() throws Exception {
		tvServer = new TVServer(tvPort);
		tvServer.setRun(true);
		tvServer.start();
		System.out.println("Running test for checkAliveFalse");
		tvServer.setStatusCode("HTTP/1.1 404 Not found");
		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean checkAlive = panasonicTVBinding.checkAlive(tvIp);

		assertFalse(checkAlive);
	}

	@Test
	public void testCheckAliveTVOffline() throws Exception {
		System.out.println("Running test for testCheckAliveTVOffline");

		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		boolean checkAlive = panasonicTVBinding.checkAlive(tvIp);
		assertFalse(checkAlive);
	}

	@Test
	public void testSendHDMICommand() throws Exception {
		System.out.println("Running test for testSendHDMICommand()");
		tvServer = new TVServer(tvPort);
		tvServer.setRun(true);
		tvServer.start();

		tvServer.setStatusCode("HTTP/1.1 200 OK");

		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		int statusCode = panasonicTVBinding.sendCommand(tvIp, "HDMI");
		assertEquals(statusCode, 200);
	}

	@Test
	public void testSendtToEmptyIP() throws Exception {
		System.out.println("Running test for testSendtToEmptyIP()");

		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		int statusCode = panasonicTVBinding.sendCommand(null, "");
		assertEquals(statusCode, 0);
	}

	
}
