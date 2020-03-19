package cm.homeautomation.services.actor;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;

@ApplicationScoped
@ServerEndpoint(value = "/actor/{clientId}", configurator = ActorEndpointConfigurator.class, encoders = {
		MessageTranscoder.class }, decoders = { MessageTranscoder.class })
public class ActorEndpoint {

	private ConcurrentMap<String, Session> userSessions = new ConcurrentHashMap<>();

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
		Iterator<String> keySet = userSessions.keySet().iterator();

		while (keySet.hasNext()) {
			String key = keySet.next();

			Session session = userSessions.get(key);
			
			if (session.equals(userSession)) {
				userSessions.remove(key);
			}
		}	
	}

	public void handleEvent(String id, String status) {

		SwitchEvent switchEvent = new SwitchEvent();
		switchEvent.setSwitchId(id);
		switchEvent.setStatus(status);

		Iterator<String> keySet = userSessions.keySet().iterator();

		while (keySet.hasNext()) {
			String key = keySet.next();

			Session session = userSessions.get(key);

			if (session.isOpen()) {
				try {
					LogManager.getLogger(this.getClass()).info("Actor Sending to {}", session.getId());
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
		// we don't react on messages
	}

	public ConcurrentMap<String, Session> getUserSessions() {
		return userSessions;
	}

	public void setUserSessions(ConcurrentMap<String, Session> userSessions) {
		this.userSessions = userSessions;
	}
}
