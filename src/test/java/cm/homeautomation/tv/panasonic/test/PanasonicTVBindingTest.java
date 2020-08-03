package cm.homeautomation.tv.panasonic.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.tv.panasonic.PanasonicTVBinding;

public class PanasonicTVBindingTest {

	private final int tvPort = 55000;
	private final String tvIp = "127.0.0.1";
	private TVServer tvServer;

	
	@BeforeEach
	public void setup() {
		tearDown();
	}

	@AfterEach
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
		assertEquals(200, statusCode);
	}

	@Test
	public void testSendtToEmptyIP() throws Exception {
		System.out.println("Running test for testSendtToEmptyIP()");

		PanasonicTVBinding panasonicTVBinding = new PanasonicTVBinding();
		int statusCode = panasonicTVBinding.sendCommand(null, "");
		assertEquals(0, statusCode);
	}

	
}
