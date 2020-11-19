package cm.homeautomation.device;

import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import cm.homeautomation.entities.LinkedDevices;

public class LinkedDevicesService {

	@Inject
	EntityManager em;

	public Map<Long, String> getLinkedDevice(Long id) {

		LinkedDevices linkedDevice = em.find(LinkedDevices.class, id);

		return linkedDevice.getLinkedDevices();
	}

}
