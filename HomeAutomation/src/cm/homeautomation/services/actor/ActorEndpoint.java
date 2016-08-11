package cm.homeautomation.services.actor;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/actor/{clientId}", configurator = ActorEndpointConfigurator.class, encoders = {
		MessageTranscoder.class }, decoders = { MessageTranscoder.class })
public class ActorEndpoint {

	private ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<String, Session>();

	/**
	 * Callback hook for Connection open events. This method will be invoked
	 * when a client requests for a WebSocket connection.
	 * 
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(@PathParam("clientId") String clientId, Session userSession) {
		userSessions.put(clientId, userSession);
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

		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			if (session.isOpen()) {
				try {
					System.out.println("Actor Sending to " + session.getId());
					session.getAsyncRemote().sendObject(switchEvent);
				} catch (IllegalStateException e) {
					userSessions.remove(key);
				}
			} else {
				userSessions.remove(key);
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session userSession) {

	}
}
