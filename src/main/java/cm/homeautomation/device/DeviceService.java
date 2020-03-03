package cm.homeautomation.device;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

public class DeviceService {

	private static final String MAC = "Mac: {}";

	private DeviceService() {
		// do nothing
	}

	public static Device getDeviceForMac(String mac) {
		if (mac == null) {
			return null;
		}

		try {
			final EntityManager em = EntityManagerService.getNewManager();
			mac = mac.toLowerCase();
			LogManager.getLogger(DeviceService.class).info(MAC, mac);
			final List<Device> devices = em.createQuery("select d from Device d where d.mac=:mac", Device.class)
					.setParameter("mac", mac).getResultList();

			if ((devices != null) && !devices.isEmpty()) {
				return devices.get(0);
			}
			return null;
		} catch (final NoResultException e) {
			LogManager.getLogger(DeviceService.class).error(MAC + mac, e);
			return null;
		}
	}

	public static Room getRoomForMac(final String mac) {
		try {
			final Device device = getDeviceForMac(mac);

			if (device == null) {
				LogManager.getLogger(DeviceService.class).error("No Device for mac found: {}",  mac);
				return null;
			}

			return device.getRoom();
		} catch (final NoResultException e) {
			LogManager.getLogger(DeviceService.class).info(MAC + mac, e);

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
