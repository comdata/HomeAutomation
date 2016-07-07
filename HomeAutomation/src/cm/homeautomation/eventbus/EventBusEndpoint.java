package cm.homeautomation.eventbus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.services.actor.MessageTranscoder;

@ServerEndpoint(value = "/eventbus", configurator = EventBusEndpointConfigurator.class, encoders = {
		MessageTranscoder.class }, decoders = { MessageTranscoder.class })
public class EventBusEndpoint {

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

	@Subscribe
	public void handleEvent(Object eventPayload) {
		
		EventObject eventObject = new EventObject(eventPayload);
		
		for (Session session : userSessions) {
			System.out.println("Sending to " + session.getId());
			session.getAsyncRemote().sendObject(eventObject);
		}
	}
	
	@OnMessage
	public void onMessage(String message, Session userSession) {

		
	}
}
