package cm.homeautomation.networkMonitor;

import java.util.Date;
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
			List<NetworkDevice> resultList = null; 
			
			String mac = networkDevice.getMac();
			
			if (mac!=null) {
				resultList = em.createQuery("select n from NetworkDevice n where n.mac=:mac").setParameter("mac", mac).getResultList();
			} else {
				resultList = em.createQuery("select n from NetworkDevice n where n.hostname=:hostname").setParameter("host", networkDevice.getHostname()()).getResultList();
			}
			
			if (resultList!=null && !resultList.isEmpty()) {
				
				NetworkDevice existingNetworkDevice = resultList.get(0);
				existingNetworkDevice.setIp(networkDevice.getIp());
				existingNetworkDevice.setHostname(networkDevice.getHostname());
				existingNetworkDevice.setMac(networkDevice.getMac());
				existingNetworkDevice.setLastSeen(new Date());
				em.getTransaction().begin();
				em.merge(existingNetworkDevice);
				em.getTransaction().commit();
			} else {
				// this is a new device, so save it
				
				em.getTransaction().begin();
				networkDevice.setLastSeen(new Date());
				em.persist(networkDevice);
				em.getTransaction().commit();
			}
			
		}
	}
	
}
