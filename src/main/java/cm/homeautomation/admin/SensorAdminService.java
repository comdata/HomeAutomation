package cm.homeautomation.admin;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.base.GenericStatus;

@Path("admin/sensor")
public class SensorAdminService {
	@Inject
	EntityManager em;

	@GET
	@Path("create/{roomId}/{name}/{type}")
	@Transactional
	public GenericStatus createSensor(@PathParam("roomId") Long roomId, @PathParam("name") String name,
			@PathParam("type") String type) {
		GenericStatus genericStatus = new GenericStatus(true);

		@SuppressWarnings("unchecked")
		List<Room> rooms = em.createQuery("select r from Room r where r.id=:roomId").setParameter("roomId", roomId)
				.getResultList();

		for (Room room : rooms) {

			Sensor sensor = new Sensor();
			sensor.setSensorName(name);
			sensor.setSensorType(type);
			sensor.setRoom(room);
			room.getSensors().add(sensor);

			em.persist(sensor);
			em.merge(room);

			genericStatus.setObject(sensor);
		}

		return genericStatus;
	}

	@GET
	@Path("update/{sensorId}/{name}/{type}")
	@Transactional
	public GenericStatus updateSensor(@PathParam("sensorId") Long sensorId, @PathParam("name") String name,
			@PathParam("type") String type) {

		@SuppressWarnings("unchecked")
		List<Sensor> sensors = em.createQuery("select s from Sensor s where s.id=:sensorId")
				.setParameter("sensorId", sensorId).getResultList();

		if (sensors != null) {

			for (Sensor sensor : sensors) {
				sensor.setSensorName(name);
				sensor.setSensorType(type);
				em.persist(sensor);
			}

		}
		return new GenericStatus(true);
	}

	@GET
	@Path("delete/{sensorId}")
	@Transactional
	public GenericStatus deleteSensor(@PathParam("sensorId") Long sensorId) {

		List<Sensor> sensors = em.createQuery("select s from Sensor s where s.id=:sensorId", Sensor.class)
				.setParameter("sensorId", sensorId).getResultList();

		if (sensors != null) {

			for (Sensor sensor : sensors) {
				Room room = sensor.getRoom();
				room.getSensors().remove(sensor);

				em.persist(room);
				em.remove(sensor);
			}
		}

		return new GenericStatus(true);
	}
}
