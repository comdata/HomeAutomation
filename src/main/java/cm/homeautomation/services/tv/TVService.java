package cm.homeautomation.services.tv;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;

import cm.homeautomation.entities.PhoneCallEvent;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.tv.panasonic.PanasonicTVBinding;
import cm.homeautomation.tv.panasonic.TVNotReachableException;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * base TV service for PanasonicTVs currently only
 *
 * @author christoph
 *
 */
@ApplicationScoped
@Path("tv/")
public class TVService extends BaseService {

	private static TVService instance;
	private static boolean muted = false;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	EventBus bus;

	public static void getCurrentStatus(final String[] args) {
		instance.internalGetCurrentStatus(args);

	}

	
	public void internalGetCurrentStatus(final String[] args) {
		final boolean aliveStatus = getInstance().getAliveStatus();

		LogManager.getLogger(TVService.class).debug("TVService got arguments: {}", Arrays.toString(args));
		LogManager.getLogger(TVService.class).debug("TV status: {}", aliveStatus);

		@SuppressWarnings("unchecked")
		final List<Switch> resultList = (em
				.createQuery("select sw from Switch sw where sw.switchType='SOCKET' and sw.subType='TV'")
				.getResultList());

		if ((resultList != null) && !resultList.isEmpty()) {

			for (final Switch singleSwitch : resultList) {
				singleSwitch.setSwitchState(aliveStatus);
				singleSwitch.setLatestStatus((aliveStatus) ? "ON" : "OFF");
				singleSwitch.setLatestStatusFrom(new Date());
				em.persist(singleSwitch);
			}

		}
	}

	public static TVService getInstance() {
		if (instance == null) {
			instance = new TVService();
		}
		return instance;
	}

	private final PanasonicTVBinding tvBinding;

	private final String tvIp;

	public TVService() {
		tvBinding = new PanasonicTVBinding();
		tvIp = configurationService.getConfigurationProperty("tv", "tvIp");
		TVService.setInstance(this);
	}

	@GET
	@Path("alive")
	public boolean getAliveStatus() {
		return tvBinding.checkAlive(tvIp);
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void phoneEventHandler(final EventObject eventObject) {

		final Object eventData = eventObject.getData();

		if (eventData instanceof PhoneCallEvent) {

			final PhoneCallEvent callEvent = (PhoneCallEvent) eventData;

			LogManager.getLogger(this.getClass()).debug("Tv IP: %s", tvIp);
			final String event = callEvent.getEvent();

			muteOrUnmuteTV(event);
		}

	}

	private void muteOrUnmuteTV(final String event) {
		if ("ring".equals(event)) {
			try {
				setMuted(true);
				tvBinding.sendCommand(tvIp, "MUTE");

				LogManager.getLogger(this.getClass()).info("muting TV");
			} catch (final TVNotReachableException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}

		if ("connect".equals(event) && !isMuted()) {
			try {
				setMuted(true);
				tvBinding.sendCommand(tvIp, "MUTE");

				LogManager.getLogger(this.getClass()).info("muting TV");
			} catch (final TVNotReachableException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}

		if ("disconnect".equals(event) && isMuted()) {
			try {
				tvBinding.sendCommand(tvIp, "MUTE");
				LogManager.getLogger(this.getClass()).info("unmuting TV");
			} catch (final TVNotReachableException e) {
				LogManager.getLogger(this.getClass()).error(e);
			}
		}
	}

	/**
	 * send a command to a TV
	 *
	 * @param command
	 * @return
	 */
	@Path("sendCommand/{command}")
	@GET
	public GenericStatus sendCommand(@PathParam("command") final String command) {

		try {
			// send the command to the TV
			tvBinding.sendCommand(tvIp, command);

			// and tell everyone about it
			final TVCommandEvent tvCommandEvent = new TVCommandEvent(tvIp, command);
			bus.publish("EventObject", new EventObject(tvCommandEvent));
		} catch (final TVNotReachableException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

		return new GenericStatus(true);
	}

	public static void setInstance(TVService instance) {
		TVService.instance = instance;
	}

	public static boolean isMuted() {
		return muted;
	}

	public static void setMuted(boolean muted) {
		TVService.muted = muted;
	}

}
