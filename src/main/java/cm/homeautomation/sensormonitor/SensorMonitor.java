package cm.homeautomation.sensormonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.messaging.HumanMessageEvent;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.eventbus.EventBus;

@Singleton
public class SensorMonitor {

	@Inject
	EventBus bus;

	@ConfigProperty(name = "sensors.monitor.oldtimeout")
	Long oldTimeout;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Scheduled(every = "300s")
	public void checkSensors() {

		String qlString = "select (select max(sd.validThru) from SensorData sd where sd.sensor=s), s from Sensor s";
		List<Object[]> resultList = em.createQuery(qlString, Object[].class).getResultList();

		Instant now = new Date().toInstant();

		for (Object[] result : resultList) {
			Date date = (Date) result[0];
			Sensor sensor = (Sensor) result[1];

			String roomName = "";

			if (sensor.getRoom() != null) {
				roomName = sensor.getRoom().getRoomName();
			}

			if (date != null) {
				Instant latestSensorDate = date.toInstant();
				Duration difference = Duration.between(latestSensorDate, now);

				if (difference.toSeconds() > oldTimeout) {

					String message = "Sensor: " + sensor.getSensorName() + " in room: " + roomName
							+ " is to old. Latest Date: " + latestSensorDate.toString();
					System.out.println(message);
					bus.publish("HumanMessageEvent", new HumanMessageEvent(message));
				}
			} else {
				String message = "Sensor: " + sensor.getSensorName() + " in room: " + roomName
						+ " has never delivered values";
				System.out.println(message);
				bus.publish("HumanMessageEvent", new HumanMessageEvent(message));
			}
		}
	}
}
