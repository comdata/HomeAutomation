package cm.homeautomation.networkMonitor;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;

public class NetworkDeviceDatabaseUpdater {

	public NetworkDeviceDatabaseUpdater() {
		EventBusService.getEventBus().register(this);
	}
	
	@Subscribe
	public void handleNetworkDeviceFound(EventObject eventObject) {
		Object data = eventObject.getData();
		if (data instanceof NetworkScannerHostFoundMessage) {
			NetworkScannerHostFoundMessage foundHostMessage=(NetworkScannerHostFoundMessage)data;
			
			NetworkDevice networkDevice = foundHostMessage.getHost();
			
			EntityManager em = EntityManagerService.getNewManager();
			
			List<NetworkDevice> resultList = em.createQuery("select n from NetworkDevice where n.ip=:ip").getResultList();
			
			if (resultList!=null && !resultList.isEmpty()) {
				
				NetworkDevice existingNetworkDevice = resultList.get(0);
				existingNetworkDevice.setHostname(networkDevice.getHostname());
				existingNetworkDevice.setMac(networkDevice.getMac());
				em.getTransaction().begin();
				em.merge(existingNetworkDevice);
				em.getTransaction().commit();
			} else {
				// this is a new device, so save it
				
				em.getTransaction().begin();
				
				em.persist(networkDevice);
				em.getTransaction().commit();
			}
			
		}
	}
	
}
