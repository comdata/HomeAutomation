package cm.homeautomation.device;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;

public class DeviceService {

	public static Long getRoomIdForMac(String mac) {
		Long roomId = null;

		if (roomId == null && mac != null) {
			EntityManager em = EntityManagerService.getNewManager();

			try {

				Device device = (Device) em.createQuery("select d from Device d where d.mac=:mac")
						.setParameter("mac", mac).getSingleResult();

				if (device == null) {

					return null;
				}

				roomId = device.getRoom().getId();

			} catch (NoResultException e) {
				return null;
			}

		}
		return roomId;
	}

	public static Room getRoomForMac(String mac) {
		Long roomId = getRoomIdForMac(mac);

		if (roomId != null) {

			EntityManager em = EntityManagerService.getNewManager();
			List<Room> roomList = em.createQuery("select r from Room r where r.id=:roomId")
					.setParameter("roomId", roomId).getResultList();

			for (Room room : roomList) {
				return room;
			}
		}
		return null;
	}

}
