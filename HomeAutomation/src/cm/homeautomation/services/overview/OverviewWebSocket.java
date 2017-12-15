package cm.homeautomation.services.overview;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

@ServerEndpoint(value = "/overview/{clientId}", configurator = OverviewEndPointConfiguration.class, encoders = {
		OverviewMessageTranscoder.class }, decoders = { OverviewMessageTranscoder.class })
public class OverviewWebSocket {
	private final ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();

	private OverviewEndPointConfiguration overviewEndPointConfiguration;
	private OverviewWebSocket overviewEndpoint;

	public OverviewWebSocket() {
		super();

		try {
			if (overviewEndPointConfiguration == null) {
				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
			}

			OverviewEndPointConfiguration.setOverviewEndpoint(this);
		} catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleSensorDataChanged(final EventObject eventObject) {

		LogManager.getLogger(this.getClass()).info("Overview got event");
		final Object eventData = eventObject.getData();
		if (eventData instanceof SensorData) {

			final SensorData sensorData = (SensorData) eventData;
			final OverviewTile overviewTileForRoom = new OverviewService()
					.getOverviewTileForRoom(sensorData.getSensor().getRoom());

			LogManager.getLogger(this.getClass())
					.info("Got eventbus for room: " + sensorData.getSensor().getRoom().getRoomName());

			try {

				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
				LogManager.getLogger(this.getClass()).info(
						"Sending tile: " + overviewTileForRoom.getRoomName() + " - " + overviewTileForRoom.getNumber());
				overviewEndpoint.sendTile(overviewTileForRoom);
			} catch (final InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LogManager.getLogger(this.getClass()).info("Eventdata not SensorData: " + eventData.getClass().getName());
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
		userSessions.remove(userSession);
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
						userSessions.remove(session);
					}
				} catch (final Exception e) {

				}
			}
		}
	}

}
