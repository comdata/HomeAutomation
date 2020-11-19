package cm.homeautomation.services.sensor.mqttsensor;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.configuration.ConfigurationService;
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

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@GET
	@Path("getAll")
	public List<MQTTTopic> getAllTopics() {

		

		return em.createQuery("select t from MQTTTopic t", MQTTTopic.class).getResultList();
	}

	@GET
	@Path("createSensor/{topicId}/{roomId}/{sensorName}")
	public GenericStatus createSensorForTopic(@PathParam("topicId") Long topicId, @PathParam("roomId") Long roomId,
			@PathParam("sensorName") String sensorName) {

		

		em.getTransaction().begin();

		MQTTTopic topic = em.find(MQTTTopic.class, topicId);
		Room room = em.find(Room.class, roomId);

		String sensorTechnicalType = "mqtt://" + topic.getTopic();

		List<Sensor> existingSensors = em
				.createQuery("select s from Sensor s where s.sensorTechnicalType=:technicalType and s.room=:room",
						Sensor.class)
				.setParameter("technicalType", sensorTechnicalType).setParameter("room", room).getResultList();

		if (existingSensors == null || existingSensors.isEmpty()) {
			Sensor sensor = Sensor.builder().sensorTechnicalType(sensorTechnicalType).room(room).sensorName(sensorName)
					.showData(true).deadbandPercent(0).reportingFactor(1f).build();

			em.persist(sensor);

			em.getTransaction().commit();

			GenericStatus genericStatus = new GenericStatus(true);
			genericStatus.setObject(sensor);
			return genericStatus;
		} else {
			return new GenericStatus(false);
		}
	}

}
