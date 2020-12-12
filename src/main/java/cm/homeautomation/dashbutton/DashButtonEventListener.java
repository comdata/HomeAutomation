package cm.homeautomation.dashbutton;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.script.ScriptException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.entities.DashButton;
import cm.homeautomation.entities.DashButtonRange;
import cm.homeautomation.entities.RemoteControl.RemoteType;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.events.RemoteControlEvent;
import cm.homeautomation.nashorn.NashornRunner;
import cm.homeautomation.services.actor.ActorPressSwitchEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import lombok.NonNull;

/**
 * listen for dashbutton events and act accordingly
 *
 * @author christoph
 *
 */
@Startup
@ApplicationScoped
@ActivateRequestContext
@Transactional(value = TxType.REQUIRES_NEW)
public class DashButtonEventListener {

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	NashornRunner nashornRunner;

	void startup(@Observes StartupEvent event) {
		System.out.println("dash listener startup");
	}

	@ConsumeEvent(value = "DashButtonEvent", blocking = true)
	public void handleEvent(DashButtonEvent dbEvent) {

		final String mac = dbEvent.getMac().replace(":", "").toUpperCase();

		System.out.println("got mac: " + mac);

		if (isDashButton(mac)) {

			DashButton dashButton = findOrCreateDashbutton(mac);
			if (dashButton != null) {
				handleDashbuttonAction(dashButton);
			}
		}
	}

	private boolean isDashButton(String mac) {
		if (mac == null) {
			throw new IllegalArgumentException("MAC is NULL");
		}
		final String vendorCode = mac.substring(0, 6);

		try {
			final DashButtonRange singleResult = em
					.createQuery("select dbr from DashButtonRange dbr where dbr.range=:vendor", DashButtonRange.class)
					.setParameter("vendor", vendorCode).getSingleResult();

			if (singleResult != null) {
				return true;
			}
		} catch (final NoResultException e) {
			// e.printStackTrace();
		}

		return false;
	}

	private void handleDashbuttonAction(@NonNull DashButton dashButton) {

		boolean dashButtonState = dashButton.isState();

		dashButton.setLastSeen(new Date());

		dashButton.setState(!dashButtonState);
		em.merge(dashButton);

		final Switch referencedSwitch = dashButton.getReferencedSwitch();
		final ScriptingEntity referencedScript = dashButton.getReferencedScript();

		RemoteControlEvent remoteControlEvent = new RemoteControlEvent(dashButton.getName(), dashButton.getMac(),
				RemoteControlEvent.EventType.REMOTE, RemoteType.DASHBUTTON);

		remoteControlEvent.setPoweredOnState(!dashButtonState);

		System.out.println("Sending RC event");
		bus.publish("RemoteControlEvent", remoteControlEvent);
		System.out.println("Sent RC event");

		if (referencedSwitch != null) {

			final String latestStatus = referencedSwitch.getLatestStatus();

			final Date latestStatusFrom = referencedSwitch.getLatestStatusFrom();

			// limit button presses to once every 10 seconds
			if (latestStatusFrom.getTime() < ((new Date()).getTime() - 10000)) {

				final String newStatus = ("ON".equals(latestStatus) ? "OFF" : "ON");

				String switchId = referencedSwitch.getId().toString();

				String message = "Dashbutton: Pressing switch {} to status: {}";

				System.out.println(message);

				LogManager.getLogger(this.getClass()).info(message, switchId, newStatus);

				bus.publish("ActorPressSwitchEvent", new ActorPressSwitchEvent(switchId, newStatus));
			}

		}

		if (referencedScript != null) {
			final String jsCode = referencedScript.getJsCode();
			try {

				nashornRunner.run(jsCode);
			} catch (final ScriptException e) {
				LogManager.getLogger(this.getClass()).error("error running code: {}", jsCode, e);
			}

		}

	}

	private DashButton findOrCreateDashbutton(final String mac) {

		final List<DashButton> resultList = em
				.createQuery("select db from DashButton db where db.mac=:mac", DashButton.class)
				.setParameter("mac", mac).getResultList();

		// create a dashbutton if it is not existing
		DashButton dashButton = null;
		if ((resultList == null) || resultList.isEmpty()) {

			dashButton = new DashButton();
			dashButton.setMac(mac);

			em.persist(dashButton);
		} else {

			dashButton = resultList.get(0);
		}
		return dashButton;
	}

}
