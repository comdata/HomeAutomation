package cm.homeautomation.dashbutton;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.eventbus.Subscribe;

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
	public void handleEvent(EventObject event) {

		Object data = event.getData();

		if (data instanceof DashButtonEvent) {

			em = EntityManagerService.getNewManager();

			DashButtonEvent dbEvent = (DashButtonEvent) data;

			String mac = dbEvent.getMac();

			List<DashButton> resultList = (List<DashButton>) em
					.createQuery("select db from DashButton db where db.mac=:mac").setParameter("mac", mac)
					.getResultList();

			DashButton dashButton = null;
			if (resultList == null || resultList.isEmpty()) {
				em.getTransaction().begin();

				dashButton = new DashButton();
				dashButton.setMac(mac);
				em.persist(dashButton);
				em.getTransaction().commit();
			} else {
				for (DashButton db : resultList) {
					dashButton = db;
					break;
				}
			}

			if (dashButton != null) {
				Switch referencedSwitch = dashButton.getReferencedSwitch();

				if (referencedSwitch != null) {

					String latestStatus = referencedSwitch.getLatestStatus();

					String newStatus = ("ON".equals(latestStatus) ? "OFF" : "ON");

					ActorService.getInstance().pressSwitch(referencedSwitch.getId().toString(), newStatus);
				}
			}

		}
	}

}
