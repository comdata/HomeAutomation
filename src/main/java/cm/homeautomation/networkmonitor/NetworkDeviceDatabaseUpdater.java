package cm.homeautomation.networkmonitor;

import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.apache.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.NetworkDevice;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;

@Startup
@Singleton
public class NetworkDeviceDatabaseUpdater {

	@ConsumeEvent(value = "NetworkScannerHostFoundMessage", blocking = true)
	public void handleNetworkDeviceFound(NetworkScannerHostFoundMessage foundHostMessage) {
		final NetworkDevice networkDevice = foundHostMessage.getHost();

		final EntityManager em = EntityManagerService.getManager();
		List<NetworkDevice> resultList = null;

		final String mac = networkDevice.getMac();
		System.out.println("got device for mac: " + mac);
//			LogManager.getLogger(this.getClass()).debug("got device for mac: "+mac);

		if (mac != null) {
			resultList = em.createQuery("select n from NetworkDevice n where n.mac=:mac", NetworkDevice.class)
					.setParameter("mac", mac).getResultList();
		}

		if ((resultList == null) || resultList.isEmpty()) {
			resultList = em.createQuery("select n from NetworkDevice n where n.ip=:ip", NetworkDevice.class)
					.setParameter("ip", networkDevice.getIp()).getResultList();
		}

		if ((resultList != null) && !resultList.isEmpty()) {

			final NetworkDevice existingNetworkDevice = resultList.get(0);
			LogManager.getLogger(this.getClass()).debug("updating existing entry: " + existingNetworkDevice.getId());
		System.out.println("updating existing entry: " + existingNetworkDevice.getId());
			existingNetworkDevice.setIp(networkDevice.getIp());
			existingNetworkDevice.setHostname(networkDevice.getHostname());
			existingNetworkDevice.setMac(networkDevice.getMac());
			existingNetworkDevice.setLastSeen(new Date());
			em.getTransaction().begin();
			em.merge(existingNetworkDevice);
			em.getTransaction().commit();
			System.out.println("done updating existing entry: "+existingNetworkDevice.getId());
//				LogManager.getLogger(this.getClass()).debug("done updating existing entry: "+existingNetworkDevice.getId());
		} else {
			// this is a new device, so save it
			System.out.println("creating new entry: "+networkDevice.getIp());
//				LogManager.getLogger(this.getClass()).debug("creating new entry: "+networkDevice.getIp());
			em.getTransaction().begin();
			networkDevice.setLastSeen(new Date());
			em.persist(networkDevice);
			em.getTransaction().commit();
			System.out.println("done creating new entry: "+networkDevice.getIp());
//				LogManager.getLogger(this.getClass()).debug("done creating new entry: "+networkDevice.getIp());
		}

	}

}
