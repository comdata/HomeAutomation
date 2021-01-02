package cm.homeautomation.services.sensor.mqttsensor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.mqtt.topicrecorder.MQTTTopicEvent;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.services.sensors.Sensors;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import lombok.NonNull;

@ApplicationScoped
@Startup
public class MQTTTopicSensorReceiver {

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	Sensors sensors;

	@Inject
	EventBus bus;

	@Inject
	ManagedExecutor executor;

	private static final Logger LOG = Logger.getLogger(MQTTTopicSensorReceiver.class);
	private Map<String, Sensor> sensorTechnicalTypeMap = new HashMap<>();

	@ConsumeEvent(value = "MQTTTopicEvent", blocking = true)
	public void receiveMQTTTopic(MQTTTopicEvent topicEvent) {

		@NonNull
		String topic = topicEvent.getTopic();
		LOG.debug("got topic: " + topic + " -  message: " + topicEvent.getMessage());

		String technicalType = "mqtt://" + topic;

		LOG.debug("looking for technicaltype: " + technicalType);
		System.out.println("topic: " + topic + " technicaltype: " + technicalType);
		Sensor sensor = sensorTechnicalTypeMap.get(technicalType);

		if (sensor != null) {
			System.out.println(sensor.getSensorName());
			LOG.debug("got sensors: " + sensor.getId() + " name:" + sensor.getSensorName());

			final SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
			sensorDataSaveRequest.setSensorId(sensor.getId());
			final SensorData sensorData = new SensorData();
			sensorData.setValue(topicEvent.getMessage());
			sensorData.setDateTime(new Date());
			sensorDataSaveRequest.setSensorData(sensorData);

			bus.publish("SensorDataSaveRequest", sensorDataSaveRequest);
		} else {
			LOG.debug("list is empty for technicaltype: " + technicalType);

		}

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
