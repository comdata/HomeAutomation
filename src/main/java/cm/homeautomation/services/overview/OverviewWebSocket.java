package cm.homeautomation.services.overview;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.eventbus.StringTranscoder;

@ApplicationScoped
@ServerEndpoint(value = "/overview/{clientId}", encoders = { OverviewMessageTranscoder.class,
		StringTranscoder.class }, decoders = { OverviewMessageTranscoder.class, StringTranscoder.class })
public class OverviewWebSocket {
	private final ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();

	private OverviewEndPointConfiguration overviewEndPointConfiguration;
	private OverviewWebSocket overviewEndpoint;

	@Inject
	OverviewService overviewService;

	public OverviewWebSocket() {
		super();

		try {
			if (overviewEndPointConfiguration == null) {
				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
			}

			OverviewEndPointConfiguration.setOverviewEndpoint(this);
		} catch (final InstantiationException e) {
			LogManager.getLogger(this.getClass()).error("Overview Websocket initialization failed", e);
		}

		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleSensorDataChanged(final EventObject eventObject) {

		LogManager.getLogger(this.getClass()).info("Overview got event");
		final Object eventData = eventObject.getData();
		if (eventData instanceof SensorData) {

			final SensorData sensorData = (SensorData) eventData;

			if (overviewService != null && sensorData != null) {
				final OverviewTile overviewTileForRoom = overviewService.updateOverviewTile(sensorData);

				if (sensorData.getSensor() != null && sensorData.getSensor().getRoom() != null) {
					LogManager.getLogger(this.getClass())
							.info("Got eventbus for room: " + sensorData.getSensor().getRoom().getRoomName());

					try {

						overviewEndPointConfiguration = new OverviewEndPointConfiguration();
						overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
						LogManager.getLogger(this.getClass()).info("Sending tile: " + overviewTileForRoom.getRoomName()
								+ " - " + overviewTileForRoom.getNumber());
						overviewEndpoint.sendTile(overviewTileForRoom);
					} catch (final InstantiationException e) {
						LogManager.getLogger(this.getClass()).error("Sending eventbus updates failed", e);
					}
				}
			} else {
				// overview not initialized
			}
		} else {
			LogManager.getLogger(this.getClass()).info("Eventdata not SensorData: " + eventData.getClass().getName());
		}
	}

	/**
	 * Callback hook for Connection close events. This method will be invoked when a
	 * client closes a WebSocket connection.
	 *
	 * @param userSession the userSession which is opened.
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

	@OnError
	public void onError(final Session session, final Throwable thr) {
		onClose(session);
	}

	@OnMessage
	public void onMessage(final String message, final Session userSession) {
		sendObjectToAllSession("{\"message\":\"pong\"}");
	}

	/**
	 * Callback hook for Connection open events. This method will be invoked when a
	 * client requests for a WebSocket connection.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(@PathParam("clientId") final String clientId, final Session userSession) {
		userSessions.put(clientId, userSession);
	}

	public void sendTile(final OverviewTile tile) {

		final Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			final String key = keySet.nextElement();

			final Session session = userSessions.get(key);

			if (session.isOpen()) {

				try {
					LogManager.getLogger(this.getClass()).info(
							"Sending to " + session.getId() + " - " + tile.getRoomName() + " - " + tile.getNumber());

					if (session.isOpen()) {

						session.getAsyncRemote().sendObject(tile);
					} else {
						userSessions.remove(key);
					}
				} catch (final Exception e) {
					LogManager.getLogger(this.getClass()).error("remove of key failed: " + key, e); //
				}
			}
		}
	}

	private void sendObjectToAllSession(final Object object) {
		final Enumeration<String> sessionKeys = userSessions.keys();

		synchronized (this) {

			while (sessionKeys.hasMoreElements()) {
				final String key = sessionKeys.nextElement();

				final Session session = userSessions.get(key);

				synchronized (session) {
					if (session.isOpen()) {
						try {
							session.getBasicRemote().sendObject(object);
							session.getBasicRemote().flushBatch();

						} catch (IllegalStateException | IOException | EncodeException e) {
							LogManager.getLogger(this.getClass())
									.error("Sending failed" + session.getId() + " key: " + key, e); //
							userSessions.remove(key);
						}
					} else {
						userSessions.remove(key);
					}
				}

			}
		}
	}

}
