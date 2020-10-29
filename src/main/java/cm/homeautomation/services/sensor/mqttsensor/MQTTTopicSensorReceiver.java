package cm.homeautomation.services.sensor.mqttsensor;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.greenrobot.eventbus.Subscribe;
import org.jboss.logging.Logger;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;

@ApplicationScoped
@AutoCreateInstance
public class MQTTTopicSensorReceiver {

	private static final Logger LOG = Logger.getLogger(MQTTTopicSensorReceiver.class);

	public MQTTTopicSensorReceiver() {
		EventBusService.getEventBus().register(this);
	}

	@Subscribe
	public void receiveMQTTTopic(MQTTTopicEvent topicEvent) {
		Runnable mqttTopicThread = () -> {
		LOG.debug("got topic: " + topicEvent.getTopic() + " -  message: " + topicEvent.getMessage());

		EntityManager em = EntityManagerService.getManager();

		String technicalType = "mqtt://" + topicEvent.getTopic();
		try {

			LOG.debug("looking for technicaltype: " + technicalType);

			List<Sensor> sensors = em
					.createQuery("select s from Sensor s where s.sensorTechnicalType=:technicalType", Sensor.class)
					.setParameter("technicalType", technicalType).getResultList();

			if (sensors != null && !sensors.isEmpty()) {

				try {

					Sensor sensor = sensors.get(0);
					LOG.debug("got sensors: " + sensor.getId() + " name:" + sensor.getSensorName());

					Sensors.getInstance().saveSensorData(sensor.getId(), topicEvent.getMessage());
				} catch (SensorDataLimitViolationException e) {
					LOG.error(e);
				}
			} else {
				LOG.debug("list is empty for technicaltype: " + technicalType);

			}

		} catch (NoResultException e) { // ignore since no result is a valid expectation here
			LOG.debug(e);
		}
		};
		new Thread(mqttTopicThread).start();

	}

}
