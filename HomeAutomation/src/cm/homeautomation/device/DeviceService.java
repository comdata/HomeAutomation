package cm.homeautomation.device;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class DeviceService {

	public static Long getRoomIdForMac(String mac) {
		Long roomId = null;

		Room room = getRoomForMac(mac);
		if (room != null) {
			roomId = room.getId();
		}
		return roomId;
	}

	public static Room getRoomForMac(String mac) {
		try {
			Device device = getDeviceForMac(mac);

			if (device==null) {
				return null;
			}
			
			return device.getRoom();
		} catch (NoResultException e) {
			LogManager.getLogger(DeviceService.class).info("Mac: " + mac, e);

			return null;
		}
	}

	public static Device getDeviceForMac(String mac) {
		try {
			EntityManager em = EntityManagerService.getNewManager();
			LogManager.getLogger(DeviceService.class).info("Mac: " + mac);
			@SuppressWarnings("unchecked")
			List<Device> devices = (List<Device>) em.createQuery("select d from Device d where d.mac=:mac").setParameter("mac", mac)
					.getResultList();

			em.close();
			
			if (devices != null && devices.isEmpty()) {
				for (Device device : devices) {
					return device;
				}
			}
			return null;
		} catch (NoResultException e) {
			LogManager.getLogger(DeviceService.class).info("Mac: " + mac, e);
			return null;
		}
	}

}
