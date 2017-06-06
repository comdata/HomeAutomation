package cm.homeautomation.eventbus;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

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

@ServerEndpoint(value = "/eventbus/{clientId}", configurator = EventBusEndpointConfigurator.class, encoders = {
		EventTranscoder.class }, decoders = { EventTranscoder.class })
public class EventBusEndpoint {

	private ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<String, Session>();

	/**
	 * register for {@link EventBus} messages
	 */
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

	/**
	 * receive EventObjects and forward them to the frontend using a web socket
	 * 
	 * @param eventObject
	 */
	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(EventObject eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			if (session.isOpen()) {
				try {
					LogManager.getLogger(this.getClass()).info("Eventbus Sending to " + session.getId());
					session.getAsyncRemote().sendObject(eventObject);
				} catch (IllegalStateException e) {
					LogManager.getLogger(this.getClass()).info("Sending failed", e);
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
