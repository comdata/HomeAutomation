package cm.homeautomation.services.sensors;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.sensors.RFEvent;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.SensorDatas;
import cm.homeautomation.sensors.SensorValue;
import cm.homeautomation.sensors.SensorValues;
import cm.homeautomation.services.base.BaseService;

@Path("sensors")
public class Sensors extends BaseService {

	@POST
	@Path("rfsniffer")
	public void registerRFEvent(RFEvent event) {
		String code = Integer.toString(event.getCode());
		System.out.println("RF Event: " + code);
		EntityManager em = EntityManagerService.getNewManager();

		Switch sw = (Switch) em.createQuery("select sw from Switch sw where sw.onCode=:code or sw.offCode=:code")
				.setParameter("code", code).getSingleResult();

		if (sw != null) {
			em.getTransaction().begin();

			String status = "";

			if (sw.getOnCode().equals(code)) {
				status = "ON";
			} else if (sw.getOffCode().equals(code)) {
				status = "OFF";
			} else {
				status = "UNKNOWN " + code;
			}

			sw.setLatestStatus(status);
			sw.setLatestStatusFrom(new Date());

			em.persist(sw);
			em.getTransaction().commit();

			// save switch changes
			Sensor sensor = sw.getSensor();
			if (sensor != null) {

				SensorDataSaveRequest sensorSaveRequest = new SensorDataSaveRequest();

				sensorSaveRequest.setSensorId(sensor.getId());
				SensorData sensorData = new SensorData();
				sensorData.setValue((("ON".equals(status)) ? "1" : "0"));
				sensorSaveRequest.setSensorData(sensorData);

				this.saveSensorData(sensorSaveRequest);
			}

		}

	}

	@POST
	@Path("forroom/save")
	public void saveSensorData(SensorDataSaveRequest request) {
		EntityManager em = EntityManagerService.getNewManager();

		Object singleResult = em.createQuery("select s from Sensor s where s.id=:sensorId")
				.setParameter("sensorId", request.getSensorId()).getSingleResult();

		if (singleResult instanceof Sensor) {
			em.getTransaction().begin();
			Sensor sensor = (Sensor) singleResult;

			SensorData sensorData;

			List existingSensorDataList = em
					.createQuery(
							"select sd from SensorData sd where sd.sensor IN (select s from Sensor s where s.id=:sensorId) order by sd.dateTime desc")
					.setMaxResults(1).setParameter("sensorId", request.getSensorId()).getResultList();

			SensorData existingSensorData = null;
			if (!existingSensorDataList.isEmpty()) {
				existingSensorData = (SensorData) existingSensorDataList.get(0);
			}

			SensorData requestSensorData = request.getSensorData();
			if (existingSensorData != null && existingSensorData.getValue().equals(requestSensorData.getValue())) {
				existingSensorData.setValidThru(new Date());
				em.merge(existingSensorData);
			} else {
				if (existingSensorData!=null && requestSensorData != null && requestSensorData.getDateTime() != null) {
					existingSensorData.setValidThru(new Date(requestSensorData.getDateTime().getTime() - 1000));
					em.merge(existingSensorData);
				}
				

				sensorData = requestSensorData;
				sensorData.setSensor(sensor);
				em.persist(sensorData);
			}

			em.getTransaction().commit();
		} else {
			System.err.println("Not a sensor");
		}
	}

	@Path("forroom/{room}")
	@GET
	public SensorDatas getDataForRoom(@PathParam("room") String room) {
		SensorDatas sensorDatas = new SensorDatas();

		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<Object> sensors = em
				.createQuery("select s FROM Sensor s where s.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		Date now = new Date();

		for (Object object : sensors) {
			if (object instanceof Sensor) {
				Sensor sensor = (Sensor) object;

				SensorValues sensorData = new SensorValues();

				sensorData.setSensorName(sensor.getSensorName());

				Date twoDaysAgo = new Date((new Date()).getTime() - (86400 * 1000));
				@SuppressWarnings("unchecked")
				List<Object> data = em
						.createQuery("select sd from SensorData sd where sd.sensor=:sensor and sd.dateTime>=:timeframe")
						.setParameter("sensor", sensor).setParameter("timeframe", twoDaysAgo).getResultList();

				String latestValue = "";
				SensorValue lastSensorValue = null;
				for (Object dataObject : data) {
					if (dataObject instanceof SensorData) {
						SensorData sData = (SensorData) dataObject;

						if (lastSensorValue != null) {
							SensorValue tempSensorValue = new SensorValue();
							tempSensorValue.setValue(lastSensorValue.getValue());
							tempSensorValue.setDateTime(new Date(sData.getDateTime().getTime() - 1000));
							sensorData.getValues().add(tempSensorValue);
						}

						SensorValue sensorValue = new SensorValue();
						sensorValue.setDateTime(sData.getDateTime());
						sensorValue.setValue(sData.getValue());

						latestValue = sData.getValue();
						lastSensorValue = sensorValue;
						sensorData.getValues().add(sensorValue);
					}
				}

				// add a last value for the charts
				SensorValue latestInterpolatedValue = new SensorValue();
				latestInterpolatedValue.setDateTime(now);
				latestInterpolatedValue.setValue(latestValue);
				sensorData.getValues().add(latestInterpolatedValue);

				sensorDatas.getSensorData().add(sensorData);
			}

		}

		return sensorDatas;
	}
}
