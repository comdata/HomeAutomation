package cm.homeautomation.services.sensors;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.RFEvent;
import cm.homeautomation.sensors.SensorDataRoomSaveRequest;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.SensorDatas;
import cm.homeautomation.sensors.SensorValue;
import cm.homeautomation.sensors.SensorValues;
import cm.homeautomation.services.actor.SwitchEvent;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

@Path("sensors")
public class Sensors extends BaseService {

	class DataLoadThread extends Thread {
		private SensorDatas sensorDatas;
		private final EntityManager em;
		private final Date now;
		private final Object object;
		private final CountDownLatch latch;

		public DataLoadThread(final SensorDatas sensorDatas, final EntityManager em, final Date now,
				final Object object, final CountDownLatch latch) {
			this.sensorDatas = sensorDatas;
			this.em = em;
			this.now = now;
			this.object = object;
			this.latch = latch;

		}

		public SensorDatas getSensorDatas() {
			return sensorDatas;
		}

		@Override
		public void run() {
			loadSensorData(sensorDatas, em, now, object);
			this.latch.countDown();
			this.stop();
		}

		public void setSensorDatas(final SensorDatas sensorDatas) {
			this.sensorDatas = sensorDatas;
		}
	}

	/**
	 * load sensor data for a room
	 *
	 * @param room
	 * @return
	 */
	@Path("forroom/{room}")
	@GET
	public SensorDatas getDataForRoom(@PathParam("room") final String room) {
		final SensorDatas sensorDatas = new SensorDatas();

		final EntityManager em = EntityManagerService.getNewManager();
		// em.getTransaction().begin();
		@SuppressWarnings("unchecked")
		final List<Object> sensors = em.createQuery(
				"select s FROM Sensor s where s.showData=true and s.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		final Date now = new Date();

		final CountDownLatch latch = new CountDownLatch(sensors.size());

		for (final Object object : sensors) {
			final DataLoadThread dataLoadThread = new DataLoadThread(sensorDatas, em, now, object, latch);
			dataLoadThread.start();
		}

		try {
			latch.await();
			// em.getTransaction().commit();
		} catch (final InterruptedException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}
		try {
			em.close();
		} catch (final IllegalStateException e) {

		}
		return sensorDatas;
	}

	/**
	 * TODO refactor filtering
	 *
	 * @param sensorDatas
	 * @param em
	 * @param now
	 * @param object
	 */
	public void loadSensorData(final SensorDatas sensorDatas, final EntityManager em, final Date now,
			final Object object) {
		if (object instanceof Sensor) {
			final Sensor sensor = (Sensor) object;

			final EntityManager emSensor = EntityManagerService.getNewManager();

			emSensor.getTransaction().begin();

			final SensorValues sensorData = new SensorValues();

			sensorData.setSensorName(sensor.getSensorName());

			final String queryString = "select sd from SensorData sd where sd.sensor=:sensor and sd.dateTime>=:timeframe";

			final Date twoDaysAgo = new Date((new Date()).getTime() - (86400 * 1000));
			@SuppressWarnings("unchecked")
			final List<Object> data = emSensor.createQuery(queryString).setParameter("sensor", sensor)
					.setParameter("timeframe", twoDaysAgo).getResultList();

			String latestValue = "";
			SensorValue lastSensorValue = null;

			// filter pressure by 0.2 percent changes only
			if ("PRESSURE".equals(sensor.getSensorType())) {
				for (final Object dataObject : data) {
					if (dataObject instanceof SensorData) {
						final SensorData sData = (SensorData) dataObject;

						final String currentValue = sData.getValue().replace(",", ".");
						String lastValue = "0";

						if (lastSensorValue != null) {
							lastValue = lastSensorValue.getValue().replace(",", ".");
						}

						final double floatCurrentValue = Double.parseDouble(currentValue);

						final double floatLastValue = Double.parseDouble(lastValue);

						if (floatLastValue != floatCurrentValue) {
							final double diff = floatLastValue - floatCurrentValue;
							final double diffAbsolute = Math.sqrt(diff * diff);

							if (diffAbsolute >= 1) {

								final SensorValue sensorValue = new SensorValue();
								sensorValue.setDateTime(sData.getDateTime());

								sensorValue.setValue(currentValue);

								latestValue = sData.getValue();
								lastSensorValue = sensorValue;
								sensorData.getValues().add(sensorValue);

							}
						}
					}
				}
			} else {
				for (final Object dataObject : data) {
					if (dataObject instanceof SensorData) {
						final SensorData sData = (SensorData) dataObject;

						if ((lastSensorValue == null)
								|| ((lastSensorValue != null) && (sData.getValue() != lastSensorValue.getValue()))) {
							if (lastSensorValue != null) {
								final SensorValue tempSensorValue = new SensorValue();
								tempSensorValue.setValue(lastSensorValue.getValue());
								tempSensorValue.setDateTime(new Date(sData.getDateTime().getTime() - 1000));
							}

							final SensorValue sensorValue = new SensorValue();
							sensorValue.setDateTime(sData.getDateTime());
							sensorValue.setValue(sData.getValue());

							latestValue = sData.getValue();
							lastSensorValue = sensorValue;
							sensorData.getValues().add(sensorValue);
						}
					}
				}
			}

			// add a last value for the charts
			final SensorValue latestInterpolatedValue = new SensorValue();
			latestInterpolatedValue.setDateTime(now);
			latestInterpolatedValue.setValue(latestValue);
			sensorData.getValues().add(latestInterpolatedValue);

			sensorDatas.getSensorData().add(sensorData);

			emSensor.getTransaction().commit();
		}

		try {
			em.close();
		} catch (final IllegalStateException e) {

		}
	}

	@POST
	@Path("rfsniffer")
	public void registerRFEvent(final RFEvent event) {
		final String code = Integer.toString(event.getCode());
		LogManager.getLogger(this.getClass()).info("RF Event: " + code);
		final EntityManager em = EntityManagerService.getNewManager();

		try {
			final Switch sw = (Switch) em
					.createQuery("select sw from Switch sw where sw.onCode=:code or sw.offCode=:code")
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

				final SwitchEvent switchEvent = new SwitchEvent();
				switchEvent.setSwitchId(Long.toString(sw.getId()));
				switchEvent.setStatus(status);

				EventBusService.getEventBus().post(switchEvent);

				sw.setLatestStatus(status);
				sw.setLatestStatusFrom(new Date());

				em.persist(sw);
				em.getTransaction().commit();

				// save switch changes
				final Sensor sensor = sw.getSensor();
				if (sensor != null) {

					final SensorDataSaveRequest sensorSaveRequest = new SensorDataSaveRequest();

					sensorSaveRequest.setSensorId(sensor.getId());
					final SensorData sensorData = new SensorData();
					sensorData.setValue((("ON".equals(status)) ? "1" : "0"));
					sensorSaveRequest.setSensorData(sensorData);

					this.saveSensorData(sensorSaveRequest);
				}

			}
		} catch (final NoResultException e) {
			LogManager.getLogger(this.getClass()).error(e);
		}

		em.close();
	}

	@POST
	@Path("save")
	public GenericStatus save(final SensorDataRoomSaveRequest request) {

		if (request == null) {
			LogManager.getLogger(this.getClass()).info("got null request");
			return new GenericStatus(false);
		}

		Long roomID = request.getRoomID();

		final EntityManager em = EntityManagerService.getNewManager();

		final String mac = request.getMac();
		if ((roomID == null) && (mac != null)) {

			roomID = DeviceService.getRoomIdForMac(mac);
			if (roomID == null) {
				// mac given but no room found
				return new GenericStatus(false);
			}
		}

		LogManager.getLogger(this.getClass()).info("Found roomId" + roomID);

		@SuppressWarnings("unchecked")
		final List<Sensor> sensorList = em
				.createQuery("select s from Sensor s where s.room=(select r from Room r where r.id=:roomId)")
				.setParameter("roomId", roomID).getResultList();

		// fix empty or wrong timestamps
		if ((request.getTimestamp() == null) || (request.getTimestamp().getTime() < (10000 * 1000))) {
			request.setTimestamp(new Date());
		}

		if (sensorList != null) {
			for (final Sensor sensor : sensorList) {

				if ("TEMPERATURE".equals(sensor.getSensorType())) {
					LogManager.getLogger(this.getClass()).info("Saving temperature to sensor: " + sensor.getId());
					saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getTemperature()),
							request.getTimestamp());
				}

				if ("HUMIDITY".equals(sensor.getSensorType())) {
					LogManager.getLogger(this.getClass()).info("Saving humidity to sensor: " + sensor.getId());
					saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getHumidity()),
							request.getTimestamp());
				}

				if ("PRESSURE".equals(sensor.getSensorType())) {
					saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getPressure()),
							request.getTimestamp());
				}

				if ("VCC".equals(sensor.getSensorType())) {
					saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getVcc()),
							request.getTimestamp());
				}
			}

		} else {
			LogManager.getLogger(this.getClass()).info("found no sensors for room " + roomID);
		}
		em.close();
		return new GenericStatus(true);
	}

	@GET
	@Path("forroom/save/{sensorId}/{value}")
	public boolean saveSensorData(@PathParam("sensorId") final Long sensorId, @PathParam("value") final String value) {
		return saveSensorDataWithTime(sensorId, value, new Date());
	}

	@POST
	@Path("forroom/save")
	public void saveSensorData(final SensorDataSaveRequest request) {
		final EntityManager em = EntityManagerService.getNewManager();

		final Object singleResult = em.createQuery("select s from Sensor s where s.id=:sensorId")
				.setParameter("sensorId", request.getSensorId()).getSingleResult();

		if (singleResult instanceof Sensor) {
			em.getTransaction().begin();
			final Sensor sensor = (Sensor) singleResult;

			SensorData sensorData;

			@SuppressWarnings("unchecked")
			final List<SensorData> existingSensorDataList = em.createQuery(
					"select sd from SensorData sd where sd.sensor IN (select s from Sensor s where s.id=:sensorId) order by sd.dateTime desc")
					.setMaxResults(1).setParameter("sensorId", request.getSensorId()).getResultList();

			SensorData existingSensorData = null;
			if (!existingSensorDataList.isEmpty()) {
				existingSensorData = existingSensorDataList.get(0);
			}

			final SensorData requestSensorData = request.getSensorData();
			final double valueAsDouble = Double.parseDouble(requestSensorData.getValue().replace(",", "."));
			final DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);

			requestSensorData.setValue(df.format(valueAsDouble).toString());
			if ((existingSensorData != null) && existingSensorData.getValue().equals(requestSensorData.getValue())) {
				existingSensorData.setValidThru(new Date());
				em.merge(existingSensorData);
				LogManager.getLogger(this.getClass()).info("Committing data: " + existingSensorData.getValue());
			} else {
				if ((existingSensorData != null) && (requestSensorData != null)
						&& (requestSensorData.getDateTime() != null)) {
					existingSensorData.setValidThru(new Date(requestSensorData.getDateTime().getTime() - 1000));
					em.merge(existingSensorData);
				}

				sensorData = requestSensorData;
				sensorData.setSensor(sensor);
				em.persist(sensorData);

				EventBusService.getEventBus().post(new EventObject(sensorData));
				LogManager.getLogger(this.getClass()).info("Committing data: " + sensorData.getValue());
			}

			em.getTransaction().commit();

		} else {
			LogManager.getLogger(this.getClass()).debug("Not a sensor");
		}
		em.close();
	}

	private boolean saveSensorDataWithTime(final Long sensorId, final String value, final Date timestamp) {
		final SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
		sensorDataSaveRequest.setSensorId(sensorId);
		final SensorData sensorData = new SensorData();
		sensorData.setValue(value);
		sensorData.setDateTime(timestamp);
		sensorDataSaveRequest.setSensorData(sensorData);

		saveSensorData(sensorDataSaveRequest);

		return true;
	}
}
