package cm.homeautomation.eventbus;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.services.actor.MessageTranscoder;

@ServerEndpoint(value = "/eventbus/{clientId}", configurator = EventBusEndpointConfigurator.class, encoders = {
		EventTranscoder.class }, decoders = { EventTranscoder.class })
public class EventBusEndpoint {

	private ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<String, Session>();

	// private Set<Session> userSessions = Collections.synchronizedSet(new
	// HashSet<Session>());

	public EventBusEndpoint() {
		EventBusService.getEventBus().register(this);
	}

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

	@Subscribe
	public void handleEvent(EventObject eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			if (session.isOpen()) {
				try {
					Logger.getLogger(this.getClass()).info("Eventbus Sending to " + session.getId());
					session.getAsyncRemote().sendObject(eventObject);
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
