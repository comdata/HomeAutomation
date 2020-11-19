package cm.homeautomation.admin;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.entities.Device;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * device administration REST service
 * 
 * @author cmertins
 *
 */
@Path("admin/device")
public class DeviceService extends BaseService {

	@Inject
	EntityManager em;
	
	/**
	 * list all devices
	 * 
	 * @return
	 */
	@GET
	@Path("getAll")
	public List<Device> getAll() {

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
	public GenericStatus createDevice(@PathParam("roomId") Long roomId, @PathParam("name") String name,
			@PathParam("mac") String mac) {


		Room room = (Room) em.createQuery("select r from Room r where r.id=:roomId").setParameter("roomId", roomId)
				.getSingleResult();

		em.getTransaction().begin();

		Device device = new Device();
		device.setMac(mac);
		device.setName(name);
		device.setRoom(room);
		room.getDevices().add(device);

		em.persist(device);
		em.persist(room);

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

	/**
	 * update a device
	 * 
	 * @param roomId
	 * @param name
	 * @param mac
	 * @return
	 */
	@GET
	@Path("update/{roomId}/{name}/{oldMac}/{newMac}")
	public GenericStatus update(@PathParam("roomId") String roomId, @PathParam("name") String name,
			@PathParam("oldMac") String mac, @PathParam("newMac") String newMac) {


		Device device = (Device) em.createQuery("select d from Device d where d.mac=:mac").setParameter("mac", mac)
				.getSingleResult();

		em.getTransaction().begin();

		device.setMac(newMac);
		device.setName(name);
		em.merge(device);

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

	/**
	 * delete device
	 * 
	 * @param mac
	 * @return
	 */
	@GET
	@Path("delete/{mac}")
	public GenericStatus delete(@PathParam("mac") String mac) {

		@SuppressWarnings("unchecked")
		List<Device> devices = em.createQuery("select d from Device d where d.mac=:mac").setParameter("mac", mac)
				.getResultList();

		em.getTransaction().begin();

		for (Device device : devices) {

			Room room = device.getRoom();
			room.getDevices().remove(device);
			em.merge(room);

			em.remove(device);

		}
		em.getTransaction().commit();

		return new GenericStatus(true);
	}
}
