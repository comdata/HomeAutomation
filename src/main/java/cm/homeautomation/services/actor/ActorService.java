package cm.homeautomation.services.actor;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.MQTTSwitch;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSender;
import cm.homeautomation.sensors.ActorMessage;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.HTTPHelper;
import cm.homeautomation.services.ir.InfraredService;
import cm.homeautomation.services.light.LightService;

/**
 * everything necessary for handling actors and reading the statuses
 *
 * @author mertins
 *
 */
@Path("actor")
public class ActorService extends BaseService implements MqttCallback {

	private final class SwitchPressRunner implements Runnable {
		private final String targetStatus;
		private final String switchId;

		private SwitchPressRunner(String targetStatus, String switchId) {
			this.targetStatus = targetStatus;
			this.switchId = switchId;
		}

		@Override
		public void run() {

			final String upperCaseTargetStatus = targetStatus.toUpperCase();

			final Switch singleSwitch = updateBackendSwitchState(switchId, upperCaseTargetStatus);

			final ActorMessage actorMessage = createActorMessage(upperCaseTargetStatus, singleSwitch);

			switchLights(targetStatus, singleSwitch);

			switchSockets(singleSwitch, actorMessage);

			EventBusService.getEventBus().post(new EventObject(actorMessage));

		}

		private ActorMessage createActorMessage(final String targetStatus, final Switch singleSwitch) {
			final ActorMessage actorMessage = new ActorMessage();
			actorMessage.setId(singleSwitch.getId());
			actorMessage.setHouseCode(singleSwitch.getHouseCode());
			actorMessage.setStatus((targetStatus.equals("ON") ? "1" : "0"));
			actorMessage.setSwitchNo(singleSwitch.getSwitchNo());

			return actorMessage;
		}

		private void switchSockets(final Switch singleSwitch, final ActorMessage actorMessage) {
			// standard lights
			if ("SOCKET".equals(singleSwitch.getSwitchType()) || "LIGHT".equals(singleSwitch.getSwitchType())) {

				if (singleSwitch instanceof MQTTSwitch) {
					MQTTSwitch singleMqttSwitch=(MQTTSwitch)singleSwitch;
					
					String topic=null;
					String message=null;
					
					if ("1".equals(actorMessage.getStatus())) {
						topic=singleMqttSwitch.getMqttPowerOnTopic();
						message=singleMqttSwitch.getMqttPowerOnMessage();
						
					} else {
						topic=singleMqttSwitch.getMqttPowerOffTopic();
						message=singleMqttSwitch.getMqttPowerOffMessage();
					}

						
						
					MQTTSender.sendMQTTMessage(topic, message);
					
				} else {

					if (singleSwitch.getHouseCode() != null) {
						sendMQTTMessage(actorMessage);
					}

					if (singleSwitch.getSwitchSetUrl() != null) {
						sendHTTPMessage(singleSwitch, actorMessage);
					}
				}
			} else if ("IR".equals(singleSwitch.getSwitchType())) {
				try {
					InfraredService.getInstance().sendCommand(singleSwitch.getIrCommand().getId());
				} catch (final JsonProcessingException e) {
					LogManager.getLogger(this.getClass()).error(e);
				}
			}
		}

		private void switchLights(String targetStatus, final Switch singleSwitch) {
			// support lights in the switches list and switch them as well
			final List<Light> lights = singleSwitch.getLights();
			if ((lights != null) && !lights.isEmpty()) {
				boolean on = false;
				if ("ON".equals(targetStatus)) {
					on = true;
				}

				final LightService lightService = LightService.getInstance();

				for (final Light light : lights) {
					if (light instanceof DimmableLight) {
						final DimmableLight dimmableLight = (DimmableLight) light;
						final int dimValue = (on) ? dimmableLight.getMaximumValue() : dimmableLight.getMinimumValue();
						lightService.dimLight(light.getId(), dimValue);
					}
				}
			}
		}

		private void sendMQTTMessage(final ActorMessage actorMessage) {

			try {
				final ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				final String jsonMessage = ow.writeValueAsString(actorMessage);

				MQTTSender.sendMQTTMessage("/switch", jsonMessage);

			} catch (final JsonProcessingException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}

		private void sendHTTPMessage(final Switch singleSwitch, final ActorMessage actorMessage) {
			String switchSetUrl = singleSwitch.getSwitchSetUrl();
			switchSetUrl = switchSetUrl.replace("{STATE}", (("0".equals(actorMessage.getStatus())) ? "off" : "on"));

			LogManager.getLogger(this.getClass()).debug(switchSetUrl);

			HTTPHelper.performHTTPRequest(switchSetUrl);
		}

	}

	private static ActorService instance;

	/**
	 * press a switch via cron
	 *
	 * @param args
	 */
	public static synchronized void cronPressSwitch(final String[] args) {
		final String switchId = args[0];
		final String status = args[1];

		ActorService.getInstance().pressSwitch(switchId, status);
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
	 * @param instance the instance to set
	 */
	public static void setInstance(final ActorService instance) {
		ActorService.instance = instance;
	}

	@Override
	public void connectionLost(final Throwable arg0) {
		// do nothing

	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken arg0) {
		// do nothing

	}

	/**
	 * get switch status for a room
	 *
	 * @param room
	 * @return
	 */
	@GET
	@Path("forroom/{room}")
	public SwitchStatuses getSwitchStatusesForRoom(@PathParam("room") final String room) {
		final EntityManager em = EntityManagerService.getNewManager();
		final SwitchStatuses switchStatuses = new SwitchStatuses();

		@SuppressWarnings("unchecked")
		final List<Switch> switchList = em.createQuery(
				"select sw from Switch sw where sw.switchType IN ('SOCKET', 'LIGHT', 'IR') and sw.visible=true and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		for (final Switch singleSwitch : switchList) {

			singleSwitch.setSwitchState("ON".equals(singleSwitch.getLatestStatus()));

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
	public SwitchStatuses getThermostatStatusesForRoom(@PathParam("room") final String room) {
		final EntityManager em = EntityManagerService.getNewManager();
		final SwitchStatuses switchStatuses = new SwitchStatuses();

		@SuppressWarnings("unchecked")
		final List<Switch> switchList = em.createQuery(
				"select sw from Switch sw where sw.switchType IN ('THERMOSTAT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		switchStatuses.getSwitchStatuses().addAll(switchList);
		em.close();
		return switchStatuses;
	}

	@Override
	public void messageArrived(final String arg0, final MqttMessage arg1) throws Exception {
		// do nothing

	}

	/**
	 * press a switch
	 *
	 * @param switchId     id of the switch
	 * @param targetStatus status ON or OFF
	 * @return
	 */
	@GET
	@Path("press/{switch}/{status}")
	public SwitchPressResponse pressSwitch(@PathParam("switch") final String switchId,
			@PathParam("status") String targetStatus) {

		final Runnable actorThread = new SwitchPressRunner(targetStatus, switchId);
		new Thread(actorThread).start();

		final SwitchPressResponse switchPressResponse = new SwitchPressResponse();
		switchPressResponse.setSuccess(true);
		return switchPressResponse;
	}

	/**
	 * press a switch from the cron scheduler
	 *
	 * @param params
	 * @return
	 */
	public synchronized SwitchPressResponse pressSwitch(final String[] params) {
		return pressSwitch(params[0], params[1]);
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
	public Switch updateBackendSwitchState(@PathParam("switch") final String switchId,
			@PathParam("status") String targetStatus) {

		targetStatus = targetStatus.toUpperCase();

		final EntityManager em = EntityManagerService.getNewManager();

		final Switch singleSwitch = (Switch) em.createQuery("select sw from Switch sw where sw.id=:switchId")
				.setParameter("switchId", Float.parseFloat(switchId)).getSingleResult();

		em.getTransaction().begin();
		singleSwitch.setLatestStatus(targetStatus);
		singleSwitch.setLatestStatusFrom(new Date());
		em.merge(singleSwitch);
		em.getTransaction().commit();

		/**
		 * post a switch information event
		 */
		final SwitchEvent switchEvent = new SwitchEvent();
		switchEvent.setStatus(targetStatus);
		switchEvent.setSwitchId(switchId);
		switchEvent.setUsedSwitch(singleSwitch);
		EventBusService.getEventBus().post(new EventObject(switchEvent));
		em.close();
		return singleSwitch;
	}

}
