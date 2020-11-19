package cm.homeautomation.services.motion;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.InfluxDBService;
import cm.homeautomation.entities.MotionDetection;
import cm.homeautomation.services.base.BaseService;
import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class MotionDetectionService extends BaseService {

	@Inject
	InfluxDBService influxDBService;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;
	
	@Inject
	InfluxDBClient influxDBClient;

	@ConsumeEvent(value = "MotionEvent", blocking = true)
	@Transactional
	public void registerMotionEvent(final MotionEvent motionEvent) {

		final boolean state = motionEvent.isState();

		System.out.println(motionEvent.getMessageString());

		List<MotionDetection> openEventList = em
				.createQuery("select m from MotionDetection m where m.externalId=:externalId and m.end is null",
						MotionDetection.class)
				.setParameter("externalId", motionEvent.getMac()).getResultList();

		if (state) {
			// motion active

			if (openEventList == null || openEventList.isEmpty()) {

				MotionDetection motionDetection = new MotionDetection();
				motionDetection.setStart(motionEvent.getTimestamp());
				motionDetection.setExternalId(motionEvent.getMac());
				motionDetection.setType(motionEvent.getType());

				em.persist(motionDetection);

			} else {
				// we already have an open event, so do nothing
			}

		} else {
			// motion stopped

			if (openEventList != null && !openEventList.isEmpty() && openEventList.size() == 1) {

				// get first element
				MotionDetection motionDetection = openEventList.get(0);
				motionDetection.setEnd(motionEvent.getTimestamp());
				em.merge(motionDetection);

			}

		}
	}

	@ConsumeEvent(value = "MotionEvent", blocking = true)
	public void saveToInflux(MotionEvent motionEvent) {
		

		try (WriteApi writeApi = influxDBClient.getWriteApi()) {

			InfluxMotionEvent influxMotionEvent = new InfluxMotionEvent();

			influxMotionEvent.setState(motionEvent.isState());
			influxMotionEvent.setRoomId(Long.valueOf(Integer.toString(motionEvent.getRoom())));
			influxMotionEvent.setExternalId(motionEvent.getMac());
			influxMotionEvent.setDateTime(motionEvent.getTimestamp().toInstant());

			writeApi.writeMeasurement(WritePrecision.MS, influxMotionEvent);
		}
	}

}
