package cm.homeautomation.dashbutton;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DashButton;
import cm.homeautomation.eventbus.EventObject;

public class DashButtonEventListener {

	@Subscribe
	public void handleEvent(EventObject event) {
	
		Object data = event.getData();
		
		if (data instanceof DashButtonEvent) {
			DashButtonEvent dbEvent = (DashButtonEvent)data;
			
			EntityManager em = EntityManagerService.getNewManager();
			
			String mac = dbEvent.getMac();
			
			List resultList = em.createQuery("select db from DashButton db where db.mac=:mac").setParameter("mac", mac).getResultList();
			
			if (resultList==null) {
				em.getTransaction().begin();

				DashButton dashButton = new DashButton();
				dashButton.setMac(mac);
				em.persist(dashButton);
				em.getTransaction().commit();
			}
		}
	}
	
}
