package cm.homeautomation.services.actor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.sensors.ActorMessage;
import cm.homeautomation.services.base.BaseService;

@Path("actor")
public class ActorService extends BaseService {

	private int port = 5000;

	@GET
	@Path("forroom/{room}")
	public SwitchStatuses getSwitchStatusesForRoom(@PathParam("room") String room) {
		EntityManager em = EntityManagerService.getNewManager();
		SwitchStatuses switchStatuses = new SwitchStatuses();

		List<Switch> switchList = em
				.createQuery("select sw from Switch sw where sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		for (Switch singleSwitch : switchList) {
			singleSwitch.setSwitchState(("ON".equals(singleSwitch.getLatestStatus())?true: false));
			switchStatuses.getSwitchStatuses().add(singleSwitch);
		}
		return switchStatuses;
	}

	public static void cronPressSwitch(String[] args) {
		String switchId=args[0];
		String status=args[0];
		
		new ActorService().pressSwitch(switchId, status);
	}
	
	@GET
	@Path("press/{switch}/{status}")
	public SwitchPressResponse pressSwitch(@PathParam("switch") String switchId, @PathParam("status") String targetStatus) {
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
		actorMessage.setStatus(status);
		actorMessage.setSwitchNo(singleSwitch.getSwitchNo());

		sendMulticastUDP(actorMessage);
		SwitchPressResponse switchPressResponse = new SwitchPressResponse();
		switchPressResponse.setSuccess(true);
		return switchPressResponse;
	}

	private void sendMulticastUDP(Object message) {
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(message);

			DatagramSocket socket = new DatagramSocket(port);

			byte[] buf = new byte[4096];
			buf = json.getBytes();

			InetAddress group = InetAddress.getByName("239.1.1.1");
			DatagramPacket packet;
			packet = new DatagramPacket(buf, buf.length, group, 5000);
			for (int i = 0; i < 10; i++) {
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

}
