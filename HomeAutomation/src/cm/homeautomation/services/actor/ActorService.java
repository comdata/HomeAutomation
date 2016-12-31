package cm.homeautomation.services.actor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.sensors.ActorMessage;
import cm.homeautomation.services.base.BaseService;

/**
 * everything necessary for handling actors and reading the statuses
 * 
 * @author mertins
 *
 */
@Path("actor")
public class ActorService extends BaseService implements MqttCallback {

	private int port = 5000;

	private static ActorService instance;

	/**
	 * get switch status for a room
	 * 
	 * @param room
	 * @return
	 */
	@GET
	@Path("forroom/{room}")
	public SwitchStatuses getSwitchStatusesForRoom(@PathParam("room") String room) {
		EntityManager em = EntityManagerService.getNewManager();
		SwitchStatuses switchStatuses = new SwitchStatuses();

		List<Switch> switchList = em
				.createQuery(
						"select sw from Switch sw where sw.switchType IN ('SOCKET', 'LIGHT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		for (Switch singleSwitch : switchList) {

			singleSwitch.setSwitchState(("ON".equals(singleSwitch.getLatestStatus()) ? true : false));

			switchStatuses.getSwitchStatuses().add(singleSwitch);
		}
		return switchStatuses;
	}

	/**
	 * get switch status for a room
	 * 
	 * @param room
	 * @return
	 */
	@GET
	@Path("thermostat/forroom/{room}")
	public SwitchStatuses getThermostatStatusesForRoom(@PathParam("room") String room) {
		EntityManager em = EntityManagerService.getNewManager();
		SwitchStatuses switchStatuses = new SwitchStatuses();

		List<Switch> switchList = em
				.createQuery(
						"select sw from Switch sw where sw.switchType IN ('THERMOSTAT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		switchStatuses.getSwitchStatuses().addAll(switchList);

		return switchStatuses;
	}

	/**
	 * press a switch via cron
	 * 
	 * @param args
	 */
	public synchronized static void cronPressSwitch(String[] args) {
		String switchId = args[0];
		String status = args[1];

		ActorService.getInstance().pressSwitch(switchId, status);
	}

	/**
	 * press a switch from the cron scheduler
	 * 
	 * @param params
	 * @return
	 */
	public synchronized SwitchPressResponse pressSwitch(String[] params) {
		return pressSwitch(params[0], params[1]);
	}

	/**
	 * press a switch
	 * 
	 * @param switchId
	 *            id of the swtich
	 * @param targetStatus
	 *            status ON or OFF
	 * @return
	 */
	@GET
	@Path("press/{switch}/{status}")
	public SwitchPressResponse pressSwitch(@PathParam("switch") String switchId,
			@PathParam("status") String targetStatus) {
		EntityManager em = EntityManagerService.getNewManager();

		Switch singleSwitch = (Switch) em.createQuery("select sw from Switch sw where sw.id=:switchId")
				.setParameter("switchId", Float.parseFloat(switchId)).getSingleResult();

		String status = "";
		if ("ON".equals(targetStatus)) {
			status = singleSwitch.getSwitchOnCode();
		} else if ("OFF".equals(targetStatus)) {
			status = singleSwitch.getSwitchOffCode();
		} else {
			// TODO fail
		}

		ActorMessage actorMessage = new ActorMessage();
		actorMessage.setHouseCode(singleSwitch.getHouseCode());
		actorMessage.setStatus((targetStatus.equals("ON") ? "1" : "0"));
		actorMessage.setSwitchNo(singleSwitch.getSwitchNo());

		sendMulticastUDP(actorMessage);
		sendMQTTMessage(actorMessage);
		
		SwitchPressResponse switchPressResponse = new SwitchPressResponse();
		switchPressResponse.setSuccess(true);
		return switchPressResponse;
	}

	private void sendMQTTMessage(ActorMessage actorMessage) {
		
	
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(actorMessage);

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			MqttClient client;
			
			client = new MqttClient("tcp://localhost:1883", "HomeAutomation/" + randomUUIDString);

			client.setCallback(this);

			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setAutomaticReconnect(true);
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(60);
			connOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			// connOpt.setUserName(M2MIO_USERNAME);
			// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

			client.connect(connOpt);
			
			MqttMessage message=new MqttMessage();
			message.setPayload(json.getBytes());
			client.publish("/switch", message);
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * send multicast message with JSON data to the Client
	 * 
	 * uses multicast group 239.1.1.1 port 5000
	 * 
	 * @param message
	 */
	private synchronized void sendMulticastUDP(Object message) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(message);
			
			DatagramSocket socket = new DatagramSocket();

			byte[] buf = new byte[4096];
			buf = json.getBytes();

			InetAddress group = InetAddress.getByName("239.1.1.1");
			DatagramPacket packet;
			packet = new DatagramPacket(buf, buf.length, group, port);
			for (int i = 0; i < 1; i++) {
				socket.send(packet);
				System.out.println("Send message:" + json);
				Thread.sleep(200);
			}

			socket.close();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return the instance
	 */
	public static ActorService getInstance() {
		if (instance == null) {
			instance = new ActorService();
		}
		return instance;
	}

	/**
	 * @param instance
	 *            the instance to set
	 */
	public static void setInstance(ActorService instance) {
		ActorService.instance = instance;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
