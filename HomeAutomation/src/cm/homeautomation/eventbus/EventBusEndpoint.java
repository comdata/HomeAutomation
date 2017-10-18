package cm.homeautomation.eventbus;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.logging.WebSocketEvent;

@ServerEndpoint(value = "/eventbus/{clientId}", configurator = EventBusEndpointConfigurator.class, encoders = {
		EventTranscoder.class,
		WebSocketEventTranscoder.class }, decoders = { EventTranscoder.class, WebSocketEventTranscoder.class })
public class EventBusEndpoint {

	private ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<String, Session>();

	/**
	 * register for {@link EventBus} messages
	 */
	public EventBusEndpoint() {
		EventBusEndpointConfigurator.setEventBusEndpoint(this);
		EventBusService.getEventBus().register(this);
	}

	/**
	 * Callback hook for Connection open events. This method will be invoked when a
	 * client requests for a WebSocket connection.
	 * 
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(@PathParam("clientId") String clientId, Session userSession) {
		userSessions.put(clientId, userSession);
	}

	/**
	 * Callback hook for Connection close events. This method will be invoked when a
	 * client closes a WebSocket connection.
	 * 
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnClose
	public void onClose(Session userSession) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			if (session.equals(userSession)) {
				userSessions.remove(key);
			}
		}
	}

	/**
	 * receive EventObjects and forward them to the frontend using a web socket
	 * 
	 * @param eventObject
	 */
	@Subscribe
	public void handleEvent(EventObject eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			synchronized (session) {
				if (session.isOpen()) {
					try {
						LogManager.getLogger(this.getClass())
								.info("Eventbus Sending to " + session.getId() + " key: " + key);

						session.getBasicRemote().sendObject(eventObject);
						session.getBasicRemote().flushBatch();
						// session.getBasicRemote().sendText("Test");

						// session.getBasicRemote().sendObject(eventObject);
					} catch (IllegalStateException | IOException | EncodeException e) {
						LogManager.getLogger(this.getClass()).info("Sending failed", e);
						// userSessions.remove(key);
					}
				} else {
					userSessions.remove(key);
				}
			}
		}
	}

	@Subscribe
	public void handleEvent(WebSocketEvent eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			synchronized (session) {
				if (session.isOpen()) {
					try {
						LogManager.getLogger(this.getClass())
								.info("Eventbus Sending to " + session.getId() + " key: " + key);

						session.getBasicRemote().sendObject(eventObject);
						session.getBasicRemote().flushBatch();

					} catch (IllegalStateException | IOException | EncodeException e) {
						LogManager.getLogger(this.getClass()).info("Sending failed", e);
						// userSessions.remove(key);
					}
				} else {
					userSessions.remove(key);
				}
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session userSession) {

	}
}
