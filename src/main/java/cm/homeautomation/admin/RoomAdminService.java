package cm.homeautomation.admin;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.services.base.BaseService;

@Path("admin/room/")
public class RoomAdminService extends BaseService {

	@Inject
	EntityManager em;
	
	@GET
	@Path("getAll")
	public RoomList getRooms() {
		RoomList roomList=new RoomList();
		
		
		List<Room> resultList = em.createQuery("select r from Room r", Room.class).getResultList();
		
		roomList.setRooms(resultList);
		
	
		
		return roomList;
	}
	
	@GET
	@Path("create/{roomName}")
	public Room createRoom(@PathParam("roomName") String roomName) {
		
		
		em.getTransaction().begin();
		Room room=new Room();
		room.setRoomName(roomName);
		em.persist(room);
		em.getTransaction().commit();
		return room;
	}
	
	@GET
	@Path("update/{roomId}/{roomName}") 
	public Room updateRoom(@PathParam("roomId") Long roomId, @PathParam("roomName") String roomName) {
		
		em.getTransaction().begin();
		Room room=(Room)em.createQuery("select r from Room r where r.id=:roomId").setParameter("roomId", roomId).getSingleResult();
		
		if (room!=null) {
			room.setRoomName(roomName);
			room=em.merge(room);
		}
		
		em.getTransaction().commit();
		
		return room;
	}
}
