package cm.homeautomation.services.actor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.fasterxml.jackson.core.JsonProcessingException;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.MQTTSwitch;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.mqtt.client.MQTTSendEvent;
import cm.homeautomation.sensors.ActorMessage;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.HTTPHelper;
import cm.homeautomation.services.ir.InfraredService;
import cm.homeautomation.services.light.LightService;
import cm.homeautomation.services.scheduler.JobArguments;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * everything necessary for handling actors and reading the statuses
 *
 * @author mertins
 *
 */
@Startup
@ApplicationScoped
@Path("actor")
@Transactional(value = TxType.REQUIRES_NEW)
public class ActorService extends BaseService {

	private static Map<Long, List<Switch>> switchList = new HashMap<>();

	private static ActorService instance;

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	LightService lightService;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	HTTPHelper httpHelper;

	public void performSwitch(String targetStatus, String switchId) throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SystemException, NotSupportedException {
		System.out.println(switchId + " - " + targetStatus);
		final String upperCaseTargetStatus = targetStatus.toUpperCase();
		System.out.println("update backend");
		final Switch singleSwitch = internalUpdateBackendSwitchState(switchId, upperCaseTargetStatus);
		System.out.println("create actor");
		final ActorMessage actorMessage = createActorMessage(upperCaseTargetStatus, singleSwitch);
		System.out.println("lights");
		switchLights(targetStatus, singleSwitch);

		System.out.println("switch");
		switchSockets(singleSwitch, actorMessage);

		bus.publish("EventObject", new EventObject(actorMessage));

	}

	void startup(@Observes StartupEvent event) {
		initSwitchList();
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

		System.out.println("type:" + singleSwitch.getSwitchType());
		// standard lights
		if ("SOCKET".equals(singleSwitch.getSwitchType()) || "LIGHT".equals(singleSwitch.getSwitchType())) {

			String debugMessage = "Actor Switch Type: " + singleSwitch.getClass().getSimpleName();
			System.out.println(debugMessage);

			if (singleSwitch instanceof MQTTSwitch) {

				MQTTSwitch singleMqttSwitch = (MQTTSwitch) singleSwitch;

				String topic = null;
				String message = null;

				if ("1".equals(actorMessage.getStatus())) {
					topic = singleMqttSwitch.getMqttPowerOnTopic();
					message = singleMqttSwitch.getMqttPowerOnMessage();

				} else {
					topic = singleMqttSwitch.getMqttPowerOffTopic();
					message = singleMqttSwitch.getMqttPowerOffMessage();
				}

				bus.publish("MQTTSendEvent", new MQTTSendEvent(topic, message));

				System.out.println("sent mqtt");

			} else {

				if (singleSwitch.getHouseCode() != null) {

					String topic = "ESP_RC/cmd";
					String message = "RC," + ("1".equals(actorMessage.getStatus()) ? "ON" : "OFF") + "="
							+ actorMessage.getHouseCode().trim() + actorMessage.getSwitchNo().trim();
					bus.publish("MQTTSendEvent", new MQTTSendEvent(topic, message));
				}

				if (singleSwitch.getSwitchSetUrl() != null) {
					sendHTTPMessage(singleSwitch, actorMessage);
				}
			}
		} else if ("IR".equals(singleSwitch.getSwitchType())) {
			try {
				InfraredService.getInstance().sendCommand(singleSwitch.getIrCommand().getId());
			} catch (final JsonProcessingException e) {

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

			for (final Light light : lights) {
				if (light instanceof DimmableLight) {
					final DimmableLight dimmableLight = (DimmableLight) light;
					final int dimValue = (on) ? dimmableLight.getMaximumValue() : dimmableLight.getMinimumValue();
					lightService.dimLight(light.getId(), dimValue);
				}
			}
		}
	}

	private void sendHTTPMessage(final Switch singleSwitch, final ActorMessage actorMessage) {
		String switchSetUrl = singleSwitch.getSwitchSetUrl();
		switchSetUrl = switchSetUrl.replace("{STATE}", (("0".equals(actorMessage.getStatus())) ? "off" : "on"));

		httpHelper.performHTTPRequest(switchSetUrl);
	}

	public ActorService() {
		instance = this;
	}

	@Scheduled(every = "120s")
	public void initSwitchList() {

		if (em != null) {
			List<Room> rooms = em.createQuery("select r from Room r", Room.class).getResultList();

			if (rooms != null && !rooms.isEmpty()) {

				for (Room room : rooms) {

					Long roomId = room.getId();
					final List<Switch> switchesList = em.createQuery(
							"select sw from Switch sw where sw.switchType IN ('SOCKET', 'LIGHT', 'IR') and sw.visible=true and sw.room=(select r from Room r where r.id=:room)",
							Switch.class).setParameter("room", roomId).getResultList();

					switchList.put(roomId, switchesList);

				}

			}
		}
	}

	/**
	 * press a switch via cron
	 *
	 * @param args
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws HeuristicRollbackException
	 * @throws HeuristicMixedException
	 * @throws RollbackException
	 * @throws IllegalStateException
	 * @throws SecurityException
	 */
	public static synchronized void cronPressSwitch(final String[] args) {
		final String switchId = args[0];
		final String status = args[1];

		ActorService.getInstance().pressSwitch(switchId, status);
	}

	@ConsumeEvent(value = "ActorService", blocking = true)
	public void consume(JobArguments arguments) {
		List<String> args=arguments.getArgumentList(); 
		final String switchId = args.get(0);
		final String status = args.get(1);

		pressSwitch(switchId, status);
	}

	/**
	 * @return the instance
	 */
	protected static ActorService getInstance() {
		if (instance == null) {
			instance = new ActorService();
		}
		return instance;
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
		final SwitchStatuses switchStatuses = new SwitchStatuses();

		List<Switch> switchesList = switchList.get(Long.parseLong(room));

		for (final Switch singleSwitch : switchesList) {

			singleSwitch.setSwitchState("ON".equals(singleSwitch.getLatestStatus()));

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
	public SwitchStatuses getThermostatStatusesForRoom(@PathParam("room") final String room) {
		final SwitchStatuses switchStatuses = new SwitchStatuses();

		@SuppressWarnings("unchecked")
		final List<Switch> switchList = em.createQuery(
				"select sw from Switch sw where sw.switchType IN ('THERMOSTAT') and sw.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		switchStatuses.getSwitchStatuses().addAll(switchList);
		return switchStatuses;
	}

	@ConsumeEvent(value = "ActorPressSwitchEvent", blocking = true)
	public void subscribePressSwitch(ActorPressSwitchEvent event) {
		System.out.println(event.getSwitchId());
		pressSwitch(event.getSwitchId(), event.getTargetStatus());
	}

	/**
	 * press a switch
	 *
	 * @param switchId     id of the switch
	 * @param targetStatus status ON or OFF
	 * @return
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws HeuristicRollbackException
	 * @throws HeuristicMixedException
	 * @throws RollbackException
	 * @throws IllegalStateException
	 * @throws SecurityException
	 */
	@GET
	@Path("press/{switch}/{status}")
	public SwitchPressResponse pressSwitch(@PathParam("switch") final String switchId,
			@PathParam("status") String targetStatus) {

		try {

			performSwitch(targetStatus, switchId);
		} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException | NotSupportedException e) {
			e.printStackTrace();
		}

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

		return internalUpdateBackendSwitchState(switchId, targetStatus);
	}

	private Switch internalUpdateBackendSwitchState(final String switchId, String targetStatus) {

		targetStatus = targetStatus.toUpperCase();
		System.out.println("Switch id" + switchId);
		Long id = Long.parseLong(switchId);
		System.out.println("loading switch " + id);
		Switch singleSwitch = null;
		try {

			singleSwitch = em.find(Switch.class, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(singleSwitch.getName());

		System.out.println("setting target status");
		singleSwitch.setTargetStatus(targetStatus);
		singleSwitch.setTargetStatusFrom(new Date());
		em.merge(singleSwitch);
		System.out.println("setting target status - post merge");

		/**
		 * post a switch information event
		 */
		final SwitchEvent switchEvent = new SwitchEvent();
		switchEvent.setStatus(targetStatus);
		switchEvent.setSwitchId(switchId);
		switchEvent.setUsedSwitch(singleSwitch);
		bus.publish("EventObject", new EventObject(switchEvent));

		return singleSwitch;

	}

}
