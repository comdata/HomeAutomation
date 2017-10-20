package cm.homeautomation.eventbus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
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
	private EventTranscoder eventTranscoder;
	private WebSocketEventTranscoder webSocketEventTranscoder;

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
	@AllowConcurrentEvents
	public void handleEvent(EventObject eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			synchronized (session) {

				if (session.isOpen()) {
					try {
						Semaphore semaphore = new Semaphore(1);
						Async async = session.getAsyncRemote();
						SendHandler handler = new SemaphoreSendHandler(semaphore, async, session);

						String text = eventTranscoder.encode(eventObject);

						LogManager.getLogger(this.getClass())
								.info("Eventbus Sending to " + session.getId() + " key: " + key + " text: " + text);

						if (async.getBatchingAllowed()) {
							async.setBatchingAllowed(false);
						}

						semaphore.acquireUninterruptibly();

						async.sendText(text, handler);
						async.flushBatch();

						// session.getBasicRemote().sendObject(eventObject);
					} catch (IllegalStateException | EncodeException | IOException e) {
						LogManager.getLogger(this.getClass()).info("Sending failed", e);
						// userSessions.remove(key);
					}
				} else {
					LogManager.getLogger(this.getClass()).info("Session not open" + key);
					// userSessions.remove(key);
				}
			}
		}
	}

	private class SemaphoreSendHandler implements SendHandler {

		private final Semaphore semaphore;
		private RemoteEndpoint remoteEndpoint;
		private Session session;

		private SemaphoreSendHandler(Semaphore semaphore, RemoteEndpoint remoteEndpoint, Session session) {
			this.semaphore = semaphore;
			this.remoteEndpoint = remoteEndpoint;
			this.session = session;
		}

		@Override
		public void onResult(SendResult result) {
			LogManager.getLogger(EventBusEndpoint.class).info("Eventbus Sent ok: " + result.isOK());
			try {
				if (session.isOpen()) {
					remoteEndpoint.flushBatch();
					remoteEndpoint.sendPing(ByteBuffer.wrap(new byte[0]));
				}
			} catch (IOException e) {
				LogManager.getLogger(this.getClass()).info("Flushing failed", e);
			}

			semaphore.release();
		}
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(WebSocketEvent eventObject) {
		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			synchronized (session) {
				if (session.isOpen()) {
					try {
						String text = webSocketEventTranscoder.encode(eventObject);
						LogManager.getLogger(this.getClass())
								.info("Eventbus Sending to " + session.getId() + " key: " + key + " text: " + text);

						// session.getBasicRemote().sendObject(eventObject);
						// session.getBasicRemote().flushBatch();

						session.getAsyncRemote().setBatchingAllowed(false);
						session.getAsyncRemote().sendText(text);
						session.getAsyncRemote().flushBatch();

					} catch (IllegalStateException | IOException | EncodeException e) {
						LogManager.getLogger(this.getClass()).info("Sending failed", e); //
						userSessions.remove(key);
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
