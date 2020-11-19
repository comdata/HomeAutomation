package cm.homeautomation.services.sensor.mqttsensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.services.sensors.SensorDataLimitViolationException;
import cm.homeautomation.services.sensors.Sensors;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
@Startup
public class MQTTTopicSensorReceiver {
	
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	Sensors sensors;

	private static final Logger LOG = Logger.getLogger(MQTTTopicSensorReceiver.class);
	private Map<String, Sensor> sensorTechnicalTypeMap = new HashMap<>();

	@ConsumeEvent(value = "MQTTTopicEvent", blocking = true)
	@Transactional
	public void receiveMQTTTopic(MQTTTopicEvent topicEvent) {
		Runnable mqttTopicThread = () -> {
			LOG.debug("got topic: " + topicEvent.getTopic() + " -  message: " + topicEvent.getMessage());

			String technicalType = "mqtt://" + topicEvent.getTopic();

			LOG.debug("looking for technicaltype: " + technicalType);

			try {

				Sensor sensor = sensorTechnicalTypeMap.get(technicalType);
				if (sensor != null) {
					LOG.debug("got sensors: " + sensor.getId() + " name:" + sensor.getSensorName());

					sensors.saveSensorData(sensor.getId(), topicEvent.getMessage());
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
