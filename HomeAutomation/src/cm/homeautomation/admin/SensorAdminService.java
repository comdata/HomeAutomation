package cm.homeautomation.admin;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.base.GenericStatus;

@Path("admin/sensor")
public class SensorAdminService {

	@GET
	@Path("create/{roomId}/{name}/{type}")
	public GenericStatus createSensor(@PathParam("roomId")Long roomId, @PathParam("name") String name, @PathParam("type") String type) {
		
		EntityManager em = EntityManagerService.getNewManager();
		
		Room room = (Room)em.createQuery("select r from Room r where id=:roomId").setParameter("roomId", roomId).getSingleResult();
		
		Sensor sensor = new Sensor();
		sensor.setSensorName(name);
		sensor.setSensorType(type);
		sensor.setRoom(room);
		room.getSensors().add(sensor);
		
		em.getTransaction().begin();
		em.persist(sensor);
		em.merge(room);
		em.getTransaction().commit();
		
		GenericStatus genericStatus = new GenericStatus(true);
		genericStatus.setObject(sensor);
		return genericStatus;
	}

	@GET
	@Path("update/{sensorId}")
	public GenericStatus updateSensor(@PathParam("sensorId") Long sensorId) {
		return new GenericStatus(true);
	}

	@GET
	@Path("delete/{sensorId}")
	public GenericStatus deleteSensor(@PathParam("sensorId") Long sensorId) {
		return new GenericStatus(true);
	}
}
