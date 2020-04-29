package cm.homeautomation.services.sensor.mqttsensor;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import lombok.extern.log4j.Log4j2;

@ApplicationScoped
@Log4j2
public class MQTTTopicSensorReceiver {

	public MQTTTopicSensorReceiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receiveMQTTTopic(MQTTTopicEvent topicEvent) {
		log.debug("got topic: " + topicEvent.getTopic() + " -  message: " + topicEvent.getMessage());

		EntityManager em = EntityManagerService.getNewManager();

		String technicalType = "mqtt://" + topicEvent.getTopic();
		try {
			
			LogManager.getLogger(this.getClass()).debug("looking for technicaltype: " + technicalType);

			List<Sensor> sensors = em
					.createQuery("select s from Sensor s where s.sensorTechnicalType=:technicalType", Sensor.class)
					.setParameter("technicalType", technicalType).getResultList();

			if (sensors != null && !sensors.isEmpty()) {

				try {
					
					Sensor sensor=sensors.get(0);
					LogManager.getLogger(this.getClass())
							.debug("got sensors: " + sensor.getId() + " name:" + sensor.getSensorName());
					
					Sensors.getInstance().saveSensorData(sensor.getId(), topicEvent.getMessage());
				} catch (SensorDataLimitViolationException e) {
					LogManager.getLogger(this.getClass()).error(e);
					log.error(e);
				}
			} else {
				LogManager.getLogger(this.getClass()).debug("list is empty for technicaltype: " + technicalType);

			}

		} catch (NoResultException e) { // ignore since no result is a valid expectation here
			log.debug(e);
		}

	}

}
