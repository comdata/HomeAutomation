package cm.homeautomation.dashbutton;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.entities.ScriptingEntity;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.nashorn.NashornRunner;
import cm.homeautomation.services.actor.ActorService;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * listen for dashbutton events and act accordingly
 *
 * @author christoph
 *
 */
@Singleton
public class DashButtonEventListener {

	@Inject
	EventBus bus;

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handleEvent(final EventObject event) {

		final Object data = event.getData();

		if (data instanceof DashButtonEvent) {

			EntityManager em = EntityManagerService.getManager();

			final DashButtonEvent dbEvent = (DashButtonEvent) data;

			final String mac = dbEvent.getMac().replace(":", "").toUpperCase();

			DashButton dashButton = findOrCreateDashbutton(em, mac);

			handleDashbuttonAction(em, dashButton);

		}
	}

	private void handleDashbuttonAction(EntityManager em, DashButton dashButton) {
		if (dashButton != null) {
			em.getTransaction().begin();
			dashButton.setLastSeen(new Date());
			em.merge(dashButton);
			em.getTransaction().commit();

			final Switch referencedSwitch = dashButton.getReferencedSwitch();
			final ScriptingEntity referencedScript = dashButton.getReferencedScript();

			if (referencedSwitch != null) {

				final String latestStatus = referencedSwitch.getLatestStatus();

				final Date latestStatusFrom = referencedSwitch.getLatestStatusFrom();

				// limit button presses to once every 10 seconds
				if (latestStatusFrom.getTime() < ((new Date()).getTime() - 10000)) {

					final String newStatus = ("ON".equals(latestStatus) ? "OFF" : "ON");

					String switchId = referencedSwitch.getId().toString();

					LogManager.getLogger(this.getClass()).info("Dashbutton: Pressing switch {} to status: {}", switchId,
							newStatus);

					ActorService.getInstance().pressSwitch(switchId, newStatus);
				}

			}

			if (referencedScript != null) {
				final String jsCode = referencedScript.getJsCode();
				try {

					NashornRunner.getInstance().run(jsCode);
				} catch (final ScriptException e) {
					LogManager.getLogger(this.getClass()).error("error running code: {}", jsCode, e);
				}

			}
		}
	}

	private DashButton findOrCreateDashbutton(EntityManager em, final String mac) {

		final List<DashButton> resultList = em
				.createQuery("select db from DashButton db where db.mac=:mac", DashButton.class)
				.setParameter("mac", mac).getResultList();

		// create a dashbutton if it is not existing
		DashButton dashButton = null;
		if ((resultList == null) || resultList.isEmpty()) {
			em.getTransaction().begin();

			dashButton = new DashButton();
			dashButton.setMac(mac);

			em.persist(dashButton);
			em.getTransaction().commit();
		} else {

			dashButton = resultList.get(0);
		}
		return dashButton;
	}

}
