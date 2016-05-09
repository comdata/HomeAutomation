package cm.homeautomation.services.overview;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.zeromq.ZMQ;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.entities.SensorData;
import cm.homeautomation.eventbus.EventBus;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.SensorDataSaveRequest;

@ServerEndpoint(value = "/overview", configurator = OverviewEndPointConfiguration.class, encoders = {
		OverviewMessageTranscoder.class }, decoders = { OverviewMessageTranscoder.class })
public class OverviewWebSocket extends Thread {
	private Set<Session> userSessions = Collections.synchronizedSet(new HashSet<Session>());

	private OverviewEndPointConfiguration overviewEndPointConfiguration;
	private OverviewWebSocket overviewEndpoint;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		try {
			if (overviewEndPointConfiguration == null) {
				overviewEndPointConfiguration = new OverviewEndPointConfiguration();
				overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
			}

			ObjectMapper mapper = new ObjectMapper();
			ZMQ.Context context = ZMQ.context(1);

			// Socket to talk to server
			System.out.println("Collecting updates from weather server");
			ZMQ.Socket subscriber = context.socket(ZMQ.SUB);
			subscriber.connect("tcp://localhost:"+EventBus.ZEROMQPORT);

			subscriber.subscribe("".getBytes());
			while (!Thread.currentThread().isInterrupted()) {
				String recvStr = subscriber.recvStr(0);

				
				try {
					System.out.println("Got Event: "+recvStr);
					EventObject eventObject = mapper.readValue(recvStr, EventObject.class);

					if ("SENSOR_DATA".equals(eventObject.getEventName())) {
						Object data = eventObject.getData();
						if (data instanceof SensorData) {
							SensorData sensorData = (SensorData) data;
							forwardSensorData(overviewEndpoint, sensorData);
						}
					}
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void forwardSensorData(OverviewWebSocket overviewEndpoint, SensorData sensorData) {
		OverviewTile overviewTileForRoom = new OverviewService()
				.getOverviewTileForRoom(sensorData.getSensor().getRoom());

		overviewEndpoint.sendTile(overviewTileForRoom);

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
				System.out.println("Sending to " + session.getId());
				session.getAsyncRemote().sendObject(tile);
			} catch (Exception e) {

			}
		}
	}

}
