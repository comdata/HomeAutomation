package cm.homeautomation.device;

import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;

public class DeviceService {

	public static Long getRoomIdForMac(String mac) {
		Long roomId=null;
		
		if (roomId==null && mac!=null) {
			EntityManager em=EntityManagerService.getNewManager();
			
			Device device = (Device) em.createQuery("select d from Device d where d.mac=:mac").setParameter("mac", mac).getSingleResult();
			
			if (device==null) {
				
				return null;
			}
			
			roomId=device.getRoom().getId();
		}
		return roomId;
	}
	
}
