package cm.homeautomation.device;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class DeviceService {

	public static Device getDeviceForMac(String mac) {
		if (mac == null) {
			return null;
		}

		try {
			final EntityManager em = EntityManagerService.getNewManager();
			mac = mac.toLowerCase();
			LogManager.getLogger(DeviceService.class).info("Mac: " + mac);
			@SuppressWarnings("unchecked")
			final List<Device> devices = em.createQuery("select d from Device d where d.mac=:mac")
					.setParameter("mac", mac).getResultList();

			if ((devices != null) && !devices.isEmpty()) {
				for (final Device device : devices) {
					return device;
				}
			}

			em.getTransaction().begin();
			final Device device = new Device();
			device.setName("unnamed");
			device.setMac(mac);
			em.persist(device);

			em.getTransaction().commit();

			em.close();
			return null;
		} catch (final NoResultException e) {
			LogManager.getLogger(DeviceService.class).error("Mac: " + mac, e);
			return null;
		}
	}

	public static Room getRoomForMac(final String mac) {
		try {
			final Device device = getDeviceForMac(mac);

			if (device == null) {
				LogManager.getLogger(DeviceService.class).error("No Device for mac found: " + mac);
				return null;
			}

			return device.getRoom();
		} catch (final NoResultException e) {
			LogManager.getLogger(DeviceService.class).info("Mac: " + mac, e);

			return null;
		}
	}

	public static Long getRoomIdForMac(final String mac) {
		Long roomId = null;

		final Room room = getRoomForMac(mac);
		if (room != null) {
			roomId = room.getId();
		}
		return roomId;
	}

}
