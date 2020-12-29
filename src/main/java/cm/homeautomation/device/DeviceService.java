package cm.homeautomation.device;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;

@ApplicationScoped
public class DeviceService {
	
	@Inject
	EntityManager em;

	private DeviceService() {
		// do nothing
	}

	public Device getDeviceForMac(String mac) {
		if (mac == null) {
			return null;
		}

		try {
			mac = mac.toLowerCase();
//			//LogManager.getLogger(DeviceService.class).info(MAC, mac);
			final List<Device> devices = em.createQuery("select d from Device d where d.mac=:mac", Device.class)
					.setParameter("mac", mac).getResultList();

			if ((devices != null) && !devices.isEmpty()) {
				return devices.get(0);
			}
			return null;
		} catch (final NoResultException e) {
//			//LogManager.getLogger(DeviceService.class).debug(MAC, mac, e);
			return null;
		}
	}

	public Room getRoomForMac(final String mac) {
		try {
			final Device device = getDeviceForMac(mac);

			if (device == null) {
//				//LogManager.getLogger(DeviceService.class).debug("No Device for mac found: {}", mac);
				return null;
			}

			return device.getRoom();
		} catch (final NoResultException e) {
//			//LogManager.getLogger(DeviceService.class).info(MAC, mac, e);

			return null;
		}
	}

	public Long getRoomIdForMac(final String mac) {
		Long roomId = null;

		final Room room = getRoomForMac(mac);
		if (room != null) {
			roomId = room.getId();
		}
		return roomId;
	}

}
