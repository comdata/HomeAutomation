package cm.homeautomation.dashbutton;

import java.util.List;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.actor.ActorService;

public class DashButtonEventListener {

	private EntityManager em;

	public DashButtonEventListener() {
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	public void handleEvent(final EventObject event) {

		final Object data = event.getData();

		if (data instanceof DashButtonEvent) {

			em = EntityManagerService.getNewManager();

			final DashButtonEvent dbEvent = (DashButtonEvent) data;

			final String mac = dbEvent.getMac();

			final List<DashButton> resultList = em.createQuery("select db from DashButton db where db.mac=:mac")
					.setParameter("mac", mac).getResultList();

			DashButton dashButton = null;
			if ((resultList == null) || resultList.isEmpty()) {
				em.getTransaction().begin();

				dashButton = new DashButton();
				dashButton.setMac(mac);
				em.persist(dashButton);
				em.getTransaction().commit();
			} else {
				for (final DashButton db : resultList) {
					dashButton = db;
					break;
				}
			}

			if (dashButton != null) {
				final Switch referencedSwitch = dashButton.getReferencedSwitch();

				if (referencedSwitch != null) {

					final String latestStatus = referencedSwitch.getLatestStatus();

					final String newStatus = ("ON".equals(latestStatus) ? "OFF" : "ON");

					ActorService.getInstance().pressSwitch(referencedSwitch.getId().toString(), newStatus);
				}
			}

		}
	}

}
