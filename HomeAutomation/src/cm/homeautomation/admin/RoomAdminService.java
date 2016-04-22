package cm.homeautomation.admin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("admin/room")
public class RoomAdminService extends BaseService {

	@GET
	@Path("getAll")
	public List<Room> getRooms() {
		
		EntityManager em = EntityManagerService.getNewManager();
		
		List<Room> resultList = em.createQuery("select r from Room r").getResultList();
		
		return resultList;
	}
	
	@GET
	@Path("create/{roomName}")
	public Room createRoom(@PathParam("roomName") String roomName) {
		
		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		Room room=new Room();
		room.setRoomName(roomName);
		em.persist(room);
		em.getTransaction().commit();
		return room;
	}
	
}
