package cm.homeautomation.services.overview;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

@ServerEndpoint(value = "/overview/{clientId}", configurator = OverviewEndPointConfiguration.class, encoders = {
		OverviewMessageTranscoder.class }, decoders = { OverviewMessageTranscoder.class })
public class OverviewWebSocket {
	private ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<String, Session>();

	private OverviewEndPointConfiguration overviewEndPointConfiguration;
	private OverviewWebSocket overviewEndpoint;

	public OverviewWebSocket() {
		super();

		try {
			if (overviewEndPointConfiguration == null) {
				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void handleSensorDataChanged(EventObject eventObject) {

		System.out.println("Overview got event");
		Object eventData = eventObject.getData();
		if (eventData instanceof SensorData) {

			SensorData sensorData = (SensorData) eventData;
			OverviewTile overviewTileForRoom = new OverviewService()
					.getOverviewTileForRoom(sensorData.getSensor().getRoom());

			System.out.println("Got eventbus for room: " + sensorData.getSensor().getRoom().getRoomName());

			try {

				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
				System.out.println(
						"Sending tile: " + overviewTileForRoom.getRoomName() + " - " + overviewTileForRoom.getNumber());
				overviewEndpoint.sendTile(overviewTileForRoom);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Eventdata not SensorData: " + eventData.getClass().getName());
		}
	}

	private void forwardSensorData(OverviewWebSocket overviewEndpoint, SensorData sensorData) {
		OverviewTile overviewTileForRoom = new OverviewService()
				.getOverviewTileForRoom(sensorData.getSensor().getRoom());

		try {
			if (overviewEndPointConfiguration == null) {
				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
				overviewEndpoint.sendTile(overviewTileForRoom);
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public void sendTile(OverviewTile tile) {

		Enumeration<String> keySet = userSessions.keys();

		while (keySet.hasMoreElements()) {
			String key = keySet.nextElement();

			Session session = userSessions.get(key);

			if (session.isOpen()) {

				try {
					System.out.println(
							"Sending to " + session.getId() + " - " + tile.getRoomName() + " - " + tile.getNumber());

					if (session.isOpen()) {

						session.getAsyncRemote().sendObject(tile);
					} else {
						userSessions.remove(session);
					}
				} catch (Exception e) {

				}
			}
		}
	}

}
