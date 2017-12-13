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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.logging.WebSocketEvent;

@ServerEndpoint(value = "/eventbus/{clientId}", configurator = EventBusEndpointConfigurator.class, encoders = {
		EventTranscoder.class,
		WebSocketEventTranscoder.class }, decoders = { EventTranscoder.class, WebSocketEventTranscoder.class })
public class EventBusEndpoint {

	private final ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();
	private final EventTranscoder eventTranscoder;

	private final WebSocketEventTranscoder webSocketEventTranscoder;

	/**
	 * register for {@link EventBus} messages
	 */
	public EventBusEndpoint() {
		EventBusEndpointConfigurator.setEventBusEndpoint(this);
		EventBusService.getEventBus().register(this);
		eventTranscoder = new EventTranscoder();
		webSocketEventTranscoder = new WebSocketEventTranscoder();
	}

	/**
	 * receive EventObjects and forward them to the frontend using a web socket
	 *
	 * @param eventObject
	 */
	@Subscribe
	public void handleEvent(final EventObject eventObject) {
		userSessions.keys();
		try {
			final String text = eventTranscoder.encode(eventObject);

			sendTextToAllSession(text);
			// while (keySet.hasMoreElements()) {
			// final String key = keySet.nextElement();
			//
			// final Session session = userSessions.get(key);
			//
			// synchronized (session) {
			//
			// if (session.isOpen()) {
			// try {
			// final Semaphore semaphore = new Semaphore(1);
			// final Async async = session.getAsyncRemote();
			// final SendHandler handler = new SemaphoreSendHandler(semaphore, async,
			// session);
			//
			// // LogManager.getLogger(this.getClass())
			// // .info("Eventbus Sending to " + session.getId() + " key: " + key + " text:
			// " +
			// // text);
			//
			// if (async.getBatchingAllowed()) {
			// async.setBatchingAllowed(false);
			// }
			//
			// semaphore.acquireUninterruptibly();
			//
			// async.sendText(text, handler);
			// async.flushBatch();
			// session.getBasicRemote().flushBatch();
			//
			// // session.getBasicRemote().sendObject(eventObject);
			// } catch (IllegalStateException | IOException e) {
			// LogManager.getLogger(this.getClass())
			// .error("Sending failed" + session.getId() + " key: " + key + " text: " +
			// text, e);
			// // userSessions.remove(key);
			// }
			// } else {
			// LogManager.getLogger(this.getClass()).error("Session not open" + key);
			// // userSessions.remove(key);
			// }
			// }
			// }
		} catch (final EncodeException e) {
			LogManager.getLogger(this.getClass()).error("Encoding failed", e);
		}
	}

	@Subscribe
	public void handleEvent(final WebSocketEvent eventObject) {

		try {
			final String text = webSocketEventTranscoder.encode(eventObject);

			sendTextToAllSession(text);
		} catch (final EncodeException e) {
			LogManager.getLogger(this.getClass()).error("Encoding failed", e);
		}
	}

	/**
	 * Callback hook for Connection close events. This method will be invoked when a
	 * client closes a WebSocket connection.
	 *
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnClose
	public void onClose(final Session userSession) {
		final Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			final String key = keySet.nextElement();

			final Session session = userSessions.get(key);

			if (session.equals(userSession)) {
				userSessions.remove(key);
			}
		}
	}

	@OnMessage
	public void onMessage(final String message, final Session userSession) {

	}

	/**
	 * Callback hook for Connection open events. This method will be invoked when a
	 * client requests for a WebSocket connection.
	 *
	 * @param userSession
	 *            the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(@PathParam("clientId") final String clientId, final Session userSession) {
		try {
			userSession.getBasicRemote().setBatchingAllowed(false);
		} catch (final IOException e) {
			LogManager.getLogger(this.getClass()).error("Setting batching allowed to false failed.", e); //
		}
		userSessions.put(clientId, userSession);
	}

	private void sendTextToAllSession(final String text) {
		final Enumeration<String> sessionKeys = userSessions.keys();
		while (sessionKeys.hasMoreElements()) {
			final String key = sessionKeys.nextElement();

			final Session session = userSessions.get(key);

			synchronized (session) {
				if (session.isOpen()) {
					try {

						session.getBasicRemote().setBatchingAllowed(false);
						session.getBasicRemote().sendText(text);
						session.getBasicRemote().flushBatch();

					} catch (IllegalStateException | IOException e) {
						LogManager.getLogger(this.getClass())
								.error("Sending failed" + session.getId() + " key: " + key + " text: " + text, e); //
						userSessions.remove(key);
					}
				} else {
					userSessions.remove(key);
				}
			}

		}
	}
}
