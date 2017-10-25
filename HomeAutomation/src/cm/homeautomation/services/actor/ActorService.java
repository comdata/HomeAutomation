package cm.homeautomation.services.actor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;
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
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.sensors.ActorMessage;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.HTTPHelper;
import cm.homeautomation.services.ir.InfraredService;

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

		@SuppressWarnings("unchecked")
		List<Switch> switchList = (List<Switch>) em.createQuery(
				"select sw from Switch sw where sw.switchType IN ('SOCKET', 'LIGHT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		for (Switch singleSwitch : switchList) {

			singleSwitch.setSwitchState(("ON".equals(singleSwitch.getLatestStatus()) ? true : false));

			switchStatuses.getSwitchStatuses().add(singleSwitch);
		}
		em.close();

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

		@SuppressWarnings("unchecked")
		List<Switch> switchList = (List<Switch>) em.createQuery(
				"select sw from Switch sw where sw.switchType IN ('THERMOSTAT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		switchStatuses.getSwitchStatuses().addAll(switchList);
		em.close();
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
		targetStatus = targetStatus.toUpperCase();

		Switch singleSwitch = updateBackendSwitchState(switchId, targetStatus);

		ActorMessage actorMessage = createActorMessage(targetStatus, singleSwitch);

		if ("SOCKET".equals(singleSwitch.getSwitchType()) || "LIGHT".equals(singleSwitch.getSwitchType())) {
			// sendMulticastUDP(actorMessage);
			if (singleSwitch.getHouseCode() != null) {
				sendMQTTMessage(actorMessage);
			}

			if (singleSwitch.getSwitchSetUrl() != null) {
				sendHTTPMessage(singleSwitch, actorMessage);
			}
		} else if ("IR".equals(singleSwitch.getSwitchType())) {
			try {
				InfraredService.getInstance().sendCommand(singleSwitch.getIrCommand().getId());
			} catch (JsonProcessingException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}

		SwitchPressResponse switchPressResponse = new SwitchPressResponse();
		switchPressResponse.setSuccess(true);
		return switchPressResponse;
	}

	private void sendHTTPMessage(Switch singleSwitch, ActorMessage actorMessage) {
		String switchSetUrl = singleSwitch.getSwitchSetUrl();
		switchSetUrl = switchSetUrl.replace("{STATE}", ((actorMessage.getStatus() == "0") ? "off" : "on"));

		HTTPHelper.performHTTPRequest(switchSetUrl);
	}

	private ActorMessage createActorMessage(String targetStatus, Switch singleSwitch) {
		ActorMessage actorMessage = new ActorMessage();
		actorMessage.setHouseCode(singleSwitch.getHouseCode());
		actorMessage.setStatus((targetStatus.equals("ON") ? "1" : "0"));
		actorMessage.setSwitchNo(singleSwitch.getSwitchNo());

		return actorMessage;
	}

	/**
	 * update backend switch state
	 * 
	 * @param switchId
	 * @param targetStatus
	 * @return
	 */
	@GET
	@Path("updateBackend/{switch}/{status}")
	public Switch updateBackendSwitchState(@PathParam("switch") String switchId,
			@PathParam("status") String targetStatus) {

		targetStatus = targetStatus.toUpperCase();

		EntityManager em = EntityManagerService.getNewManager();

		Switch singleSwitch = (Switch) em.createQuery("select sw from Switch sw where sw.id=:switchId")
				.setParameter("switchId", Float.parseFloat(switchId)).getSingleResult();

		em.getTransaction().begin();
		singleSwitch.setLatestStatus(targetStatus);
		singleSwitch.setLatestStatusFrom(new Date());
		em.merge(singleSwitch);
		em.getTransaction().commit();

		/**
		 * post a switch information event
		 */
		SwitchEvent switchEvent = new SwitchEvent();
		switchEvent.setStatus(targetStatus);
		switchEvent.setSwitchId(switchId);
		switchEvent.setUsedSwitch(singleSwitch);
		EventBusService.getEventBus().post(new EventObject(switchEvent));
		em.close();
		return singleSwitch;
	}

	private void sendMQTTMessage(ActorMessage actorMessage) {

		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String jsonMessage = ow.writeValueAsString(actorMessage);

			MQTTSender.sendMQTTMessage("/switch", jsonMessage);

		} catch (JsonProcessingException e) {
			LogManager.getLogger(this.getClass()).error(e);
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
		// do nothing

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// do nothing

	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// do nothing

	}

}
