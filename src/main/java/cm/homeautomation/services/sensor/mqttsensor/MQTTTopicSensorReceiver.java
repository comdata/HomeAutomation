package cm.homeautomation.services.sensor.mqttsensor;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import lombok.extern.log4j.Log4j2;

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
			Sensor sensor = em
					.createQuery("select s from Sensor s where s.sensorTechnicalType=:technicalType", Sensor.class)
					.setParameter("technicalType", technicalType).getSingleResult();

			if (sensor != null) {

				try {
					Sensors.getInstance().saveSensorData(sensor.getId(), topicEvent.getMessage());
				} catch (SensorDataLimitViolationException e) {
					log.error(e);
				}
			}

		} catch (NoResultException e) { // ignore since no result is a valid expectation here
			log.debug(e);
		}

	}

}
