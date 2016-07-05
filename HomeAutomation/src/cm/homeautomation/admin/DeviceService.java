package cm.homeautomation.admin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * device administration service
 * 
 * @author cmertins
 *
 */
@Path("admin/device")
public class DeviceService extends BaseService {

	/**
	 * list all devices
	 * 
	 * @return
	 */
	@GET
	@Path("getAll")
	public List<Device> getAll() {

		EntityManager em = EntityManagerService.getNewManager();

		@SuppressWarnings("unchecked")
		List<Device> resultList = em.createQuery("select d from Device d").getResultList();

		return resultList;
	}

	/**
	 * create a device
	 * 
	 * @param roomId
	 * @param name
	 * @param mac
	 * @return
	 */
	@GET
	@Path("create/{roomId}/{name}/{mac}")
	public GenericStatus createDevice(@PathParam("roomId") String roomId, @PathParam("name") String name,
			@PathParam("mac") String mac) {

		EntityManager em = EntityManagerService.getNewManager();

		Room room = (Room)em.createQuery("select r from Room r where id=:roomId").setParameter("roomId", roomId).getSingleResult();
		
		em.getTransaction().begin();

		Device device = new Device();
		device.setMac(mac);
		device.setName(name);
		device.setRoom(room);
		em.persist(device);

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

}
