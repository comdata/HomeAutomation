package cm.homeautomation.device;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

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

			return device.getRoom();
		} catch (NoResultException e) {
			System.out.println("Mac: " + mac);
			e.printStackTrace();
			return null;
		}
	}

	public static Device getDeviceForMac(String mac) {
		try {
			EntityManager em = EntityManagerService.getNewManager();
			System.out.println("Mac: " + mac);
			Device device = (Device) em.createQuery("select d from Device d where d.mac=:mac").setParameter("mac", mac)
					.getSingleResult();

			if (device == null) {

				return null;
			}
			return device;
		} catch (NoResultException e) {
			System.out.println("Mac: " + mac);
			e.printStackTrace();
			return null;
		}
	}

}
