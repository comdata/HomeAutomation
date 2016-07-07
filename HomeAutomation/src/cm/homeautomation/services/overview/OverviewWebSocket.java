package cm.homeautomation.services.overview;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

@ServerEndpoint(value = "/overview", configurator = OverviewEndPointConfiguration.class, encoders = {
		OverviewMessageTranscoder.class }, decoders = { OverviewMessageTranscoder.class })
public class OverviewWebSocket {
	private Set<Session> userSessions = Collections.synchronizedSet(new HashSet<Session>());

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

		
		Object eventData = eventObject.getData();
		if (eventData instanceof SensorData) {

			SensorData sensorData=(SensorData)eventData;
			OverviewTile overviewTileForRoom = new OverviewService()
					.getOverviewTileForRoom(sensorData.getSensor().getRoom());
			
			System.out.println("Got eventbus for room: "+sensorData.getSensor().getRoom().getRoomName());

			try {
				if (overviewEndPointConfiguration == null) {
					overviewEndPointConfiguration = new OverviewEndPointConfiguration();
					overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
					System.out.println("Sending tile: "+overviewTileForRoom.getRoomName()+" - "+overviewTileForRoom.getNumber());
					overviewEndpoint.sendTile(overviewTileForRoom);
				}

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	public void onOpen(Session userSession) {
		userSessions.add(userSession);
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

		for (Session session : userSessions) {
			try {
				System.out.println("Sending to " + session.getId()+" - "+tile.getRoomName()+" - "+tile.getNumber());
				session.getAsyncRemote().sendObject(tile);
			} catch (Exception e) {

			}
		}
	}

}
