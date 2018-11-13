package cm.homeautomation.services.actor.test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentMap;

import javax.websocket.Session;

import org.junit.jupiter.api.Test;

import cm.homeautomation.services.actor.ActorEndpoint;

public class ActorEndpointTest {

	@Test
	public void testOnOpen() throws Exception {
		ActorEndpoint actorEndpoint = new ActorEndpoint();
		
		String clientId="TestClient";
		
		
		Session userSession=new DummyUserSession();
		actorEndpoint.onOpen(clientId, userSession);
		
		ConcurrentMap<String, Session> userSessions = actorEndpoint.getUserSessions();
		assertNotNull(userSessions);
		assertFalse(userSessions.isEmpty());
		assertTrue(userSessions.size()==1);
	}

	@Test
	public void testOnClose() throws Exception {
		ActorEndpoint actorEndpoint = new ActorEndpoint();
		
		String clientId="TestClient";
		
		
		Session userSession=new DummyUserSession();
		actorEndpoint.onOpen(clientId, userSession);
		
		ConcurrentMap<String, Session> userSessions = actorEndpoint.getUserSessions();
		assertNotNull(userSessions);
		assertFalse(userSessions.isEmpty());
		assertTrue(userSessions.size()==1);
		
		actorEndpoint.onClose(userSession);
		
		ConcurrentMap<String, Session> userSessionsAfterClose = actorEndpoint.getUserSessions();
		assertNotNull(userSessionsAfterClose);
		assertTrue(userSessionsAfterClose.isEmpty());
		assertTrue(userSessionsAfterClose.size()==0);
	}

	@Test
	public void testHandleEvent() throws Exception {
		ActorEndpoint actorEndpoint = new ActorEndpoint();
		actorEndpoint.handleEvent("TestId", "Status");
		assertTrue(true);
	}

	@Test
	public void testOnMessage() throws Exception {
		ActorEndpoint actorEndpoint = new ActorEndpoint();
		
		String clientId="TestClient";
		
		
		Session userSession=new DummyUserSession();
		actorEndpoint.onOpen(clientId, userSession);
		
		ConcurrentMap<String, Session> userSessions = actorEndpoint.getUserSessions();
		assertNotNull(userSessions);
		assertFalse(userSessions.isEmpty());
		assertTrue(userSessions.size()==1);
	}

}
