package cm.homeautomation.device;

import java.util.Map;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.LinkedDevices;

public class LinkedDevicesService {

	
	public Map<Long, String> getLinkedDevice(Long id) {

		EntityManager em = EntityManagerService.getManager();

		LinkedDevices linkedDevice = em.find(LinkedDevices.class, id);

		return linkedDevice.getLinkedDevices();
	}
	
}
