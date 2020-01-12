package cm.homeautomation.services.sensor.mqttsensor;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MQTTTopic;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/**
 * service for the frontend to create sensors from MQTT messages
 * 
 * @author christoph
 *
 */
@Path("sensor/mqtt")
public class MQTTTopicSensorService extends BaseService {

	@GET
	@Path("getAll")
	public List<MQTTTopic> getAllTopics() {

		EntityManager em = EntityManagerService.getNewManager();

		return em.createQuery("select t from MQTTTopic t", MQTTTopic.class).getResultList();
	}

	@Path("createSensor/{topicId}/{roomId}/{sensorName}")
	public GenericStatus createSensorForTopic(@PathParam("topicId") Long topicId, @PathParam("roomId") Long roomId,
			@PathParam("sensorName") String sensorName) {

		EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		MQTTTopic topic = em.find(MQTTTopic.class, topicId);
		Room room = em.find(Room.class, roomId);

		Sensor sensor = Sensor.builder().sensorTechnicalType("mqtt://" + topic.getTopic()).room(room)
				.sensorName(sensorName).showData(true).deadbandPercent(0).build();

		em.persist(sensor);

		em.getTransaction().commit();

		return new GenericStatus(true);
	}

}
