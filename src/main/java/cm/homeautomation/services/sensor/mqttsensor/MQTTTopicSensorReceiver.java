package cm.homeautomation.services.sensor.mqttsensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.jboss.logging.Logger;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;

@Singleton
public class MQTTTopicSensorReceiver {

	private static final Logger LOG = Logger.getLogger(MQTTTopicSensorReceiver.class);
	private Map<String, Sensor> sensorTechnicalTypeMap = new HashMap<>();

	public MQTTTopicSensorReceiver() {
		initSensorMap();
	}

	@ConsumeEvent(value = "MQTTTopicEvent", blocking = true)
	public void receiveMQTTTopic(MQTTTopicEvent topicEvent) {
		Runnable mqttTopicThread = () -> {
			LOG.debug("got topic: " + topicEvent.getTopic() + " -  message: " + topicEvent.getMessage());

			String technicalType = "mqtt://" + topicEvent.getTopic();

			LOG.debug("looking for technicaltype: " + technicalType);

			try {

				Sensor sensor = sensorTechnicalTypeMap.get(technicalType);
				if (sensor != null) {
					LOG.debug("got sensors: " + sensor.getId() + " name:" + sensor.getSensorName());

					Sensors.getInstance().saveSensorData(sensor.getId(), topicEvent.getMessage());
				} else {
					LOG.debug("list is empty for technicaltype: " + technicalType);

				}
			} catch (SensorDataLimitViolationException e) {
				LOG.error(e);
			}

		};
		new Thread(mqttTopicThread).start();

	}

	@Scheduled(every = "120s")
	public void initSensorMap() {
		final EntityManager em = EntityManagerService.getManager();

		List<Sensor> sensorFullList = em.createQuery("select s from Sensor s", Sensor.class).getResultList();

		Map<String, Sensor> sensorTechnicalTypeMapTemp = new HashMap<>();

		for (Sensor sensor : sensorFullList) {

			String sensorTechnicalType = sensor.getSensorTechnicalType();
			if (sensorTechnicalType != null && !"".equals(sensorTechnicalType)) {
				sensorTechnicalTypeMapTemp.put(sensorTechnicalType, sensor);
			}

		}
		sensorTechnicalTypeMap = sensorTechnicalTypeMapTemp;
	}

}
