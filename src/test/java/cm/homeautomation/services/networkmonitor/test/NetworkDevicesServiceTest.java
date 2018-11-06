package cm.homeautomation.services.networkmonitor.test;

import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.services.base.GenericStatus;
import cm.homeautomation.services.networkmonitor.NetworkDevicesService;

class NetworkDevicesServiceTest {

	@Test
	void testGetInstance() {
		NetworkDevicesService instance = NetworkDevicesService.getInstance();
		assertNotNull(instance);
	}

	@Test
	void testDelete() {
		EntityManager em = EntityManagerService.getNewManager();

		NetworkDevice networkDevice = new NetworkDevice();
		networkDevice.setMac("00:00:00:00:ff");
		networkDevice.setHostname("DeleteTestHost"+System.currentTimeMillis());
		networkDevice.setIp("1.1.1.1");
		em.getTransaction().begin();
		
		em.persist(networkDevice);
		
		em.getTransaction().commit();
		
		GenericStatus status = NetworkDevicesService.getInstance().delete(networkDevice.getHostname(), networkDevice.getIp(), networkDevice.getMac());

		assertTrue(status.isSuccess());
		
	}

	@Test
	void testReadAll() {
	}

	@Test
	void testWakeUp() {
	}

}
