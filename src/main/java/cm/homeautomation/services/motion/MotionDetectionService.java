package cm.homeautomation.services.motion;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import cm.homeautomation.db.InfluxDBService;


import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.MotionDetection;
import cm.homeautomation.services.base.BaseService;
import io.quarkus.vertx.ConsumeEvent;

@Singleton
public class MotionDetectionService extends BaseService {

    @Inject
	InfluxDBService influxDBService;

	@ConsumeEvent(value = "MotionEvent", blocking = true)
	public void registerMotionEvent(final MotionEvent motionEvent) {

		EntityManager em = EntityManagerService.getManager();

		final boolean state = motionEvent.isState();

		System.out.println(motionEvent.getMessageString());

		List<MotionDetection> openEventList = em
				.createQuery("select m from MotionDetection m where m.externalId=:externalId and m.end is null",
						MotionDetection.class)
				.setParameter("externalId", motionEvent.getMac()).getResultList();

		if (state) {
			// motion active

			if (openEventList == null || openEventList.isEmpty()) {
				em.getTransaction().begin();

				MotionDetection motionDetection = new MotionDetection();
				motionDetection.setStart(motionEvent.getTimestamp());
				motionDetection.setExternalId(motionEvent.getMac());
				motionDetection.setType(motionEvent.getType());

				em.persist(motionDetection);
				em.getTransaction().commit();
			} else {
				// we already have an open event, so do nothing
			}

		} else {
			// motion stopped

			if (openEventList != null && !openEventList.isEmpty() && openEventList.size() == 1) {
				em.getTransaction().begin();
				// get first element
				MotionDetection motionDetection = openEventList.get(0);
				motionDetection.setEnd(motionEvent.getTimestamp());
				em.merge(motionDetection);

				em.getTransaction().commit();
			}

		}
    }
    
    @ConsumeEvent(value = "MotionEvent", blocking = true)
	public void saveToInflux(MotionEvent motionEvent) {
		InfluxDBClient influxDBClient = influxDBService.getClient();

		try (WriteApi writeApi = influxDBClient.getWriteApi()) {

			InfluxMotionEvent influxMotionEvent = new InfluxMotionEvent();

			influxMotionEvent.setState(motionEvent.isState());
            influxMotionEvent.setRoomId(motionEvent.getRoomId());
            influxMotionEvent.setExternalId(motionEvent.getMac());
			influxMotionEvent.setDateTime(motionDetectionEvent.getTimestamp().toInstant());

			writeApi.writeMeasurement(WritePrecision.MS, influxMotionEvent);
		}
	}


}
