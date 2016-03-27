package cm.homeautomation.services.actor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/actor", configurator = ActorEndpointConfigurator.class, encoders = {
		MessageTranscoder.class }, decoders = { MessageTranscoder.class })
public class ActorEndpoint {

	private Set<Session> userSessions = Collections.synchronizedSet(new HashSet<Session>());

	/**
	 * Callback hook for Connection open events. This method will be invoked
	 * when a client requests for a WebSocket connection.
	 * 
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		userSessions.add(userSession);
	}

	/**
	 * Callback hook for Connection close events. This method will be invoked
	 * when a client closes a WebSocket connection.
	 * 
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnClose
	public void onClose(Session userSession) {
		userSessions.remove(userSession);
	}

	public void handleEvent(String id, String status) {
		
		SwitchEvent switchEvent = new SwitchEvent();
		switchEvent.setSwitchId(id);
		switchEvent.setStatus(status);
		
		for (Session session : userSessions) {
			System.out.println("Sending to " + session.getId());
			session.getAsyncRemote().sendObject(switchEvent);
		}
	}
	
	@OnMessage
	public void onMessage(String message, Session userSession) {

		
	}
}
