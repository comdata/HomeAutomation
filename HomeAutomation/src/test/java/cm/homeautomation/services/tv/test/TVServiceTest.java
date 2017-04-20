package cm.homeautomation.services.tv.test;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.tv.TVService;
import cm.homeautomation.tv.panasonic.test.TVServer;

/**
 * Test the TV service
 * 
 * @author christoph
 *
 */
public class TVServiceTest {
	private final int tvPort = 55000;
	
	private TVService tvService;
	private TVServer tvServer;

	@Before
	public void setup() {
		tvService = new TVService();
		
		tvServer = new TVServer(tvPort);
		tvServer.setRun(true);
		tvServer.start();
	}
	
	@After
	public void teardown() {
		tvServer.setRun(false);
		tvServer.stop();
	}
	
	
	@Test
	public void testSendCommand() {
		tvServer.setStatusCode("HTTP/1.1 200 OK");
		GenericStatus sendCommand = tvService.sendCommand("MUTE");
		
		assertTrue("Send command failed", sendCommand.isSuccess());
	}

	@Test
	public void testCheckAlive() {
		tvServer.setStatusCode("HTTP/1.1 200 OK");
		boolean aliveStatus = tvService.getAliveStatus();
		assertTrue("Alive check failed", aliveStatus);
	}
	
	@Test
	public void testCheckPhoneHandlerMute() {
		PhoneCallEvent phoneEvent=new PhoneCallEvent();
		phoneEvent.setEvent("ring");
		EventObject eventObject=new EventObject(phoneEvent);
		tvService.phoneEventHandler(eventObject);
	}
	
	@Test
	public void testCheckPhoneHandlerUnMute() {
		PhoneCallEvent phoneEvent=new PhoneCallEvent();
		phoneEvent.setEvent("disconnect");
		EventObject eventObject=new EventObject(phoneEvent);
		tvService.phoneEventHandler(eventObject);
	}
}
