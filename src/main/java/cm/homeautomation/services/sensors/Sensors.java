package cm.homeautomation.services.sensors;

import java.math.RoundingMode;
import org.jboss.logging.Logger;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang3.math.NumberUtils;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.device.DeviceService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
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
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

@Startup
@ApplicationScoped
@Path("sensors")
public class Sensors extends BaseService {

	private static final Logger LOG = Logger.getLogger(Sensors.class);

	@Inject
	EventBus bus;

	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@Inject
	DeviceService deviceService;

	class DataLoadThread extends Thread {
		private SensorDatas sensorDatas;
		private final EntityManager em;
		private final Date now;
		private final Sensor sensor;
		private final CountDownLatch latch;

		public DataLoadThread(final SensorDatas sensorDatas, final EntityManager em, final Date now,
				final Sensor sensor, final CountDownLatch latch) {
			this.sensorDatas = sensorDatas;
			this.em = em;
			this.now = now;
			this.sensor = sensor;
			this.latch = latch;

		}

		public SensorDatas getSensorDatas() {
			return sensorDatas;
		}

		@Override
		public void run() {
			try {
				loadSensorData(sensorDatas, em, now, sensor);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.latch.countDown();
		}

		public void setSensorDatas(final SensorDatas sensorDatas) {
			this.sensorDatas = sensorDatas;
		}
	}

	private static Sensors instance;

	public static Sensors getInstance() {
		if (instance == null) {
			instance = new Sensors();
		}
		return instance;
	}

	private static Map<Long, Sensor> sensorsList = new HashMap<>();
	private static Map<Long, SensorData> sensorDataMap = new HashMap<>();

	public Sensors() {
		instance = this;
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

		final List<Sensor> sensors = em.createQuery(
				"select s FROM Sensor s where s.showData=true and s.room=(select r from Room r where r.id=:room)",
				Sensor.class).setParameter("room", Long.parseLong(room)).getResultList();

		final Date now = new Date();

		if (sensors != null && sensors.size() > 0) {
			final CountDownLatch latch = new CountDownLatch(sensors.size());

			for (final Sensor sensor : sensors) {
				final DataLoadThread dataLoadThread = new DataLoadThread(sensorDatas, em, now, sensor, latch);
				dataLoadThread.start();
			}

			try {
				latch.await();
			} catch (final InterruptedException e) {
//                //LOG.error(e);
			}
		}
		return sensorDatas;
	}

	/**
	 * TODO refactor filtering
	 *
	 * @param sensorDatas
	 * @param em
	 * @param now
	 * @param sensor
	 * @throws SystemException
	 * @throws NotSupportedException
	 * @throws HeuristicRollbackException
	 * @throws HeuristicMixedException
	 * @throws RollbackException
	 * @throws IllegalStateException
	 * @throws SecurityException
	 */

	public void loadSensorData(final SensorDatas sensorDatas, final EntityManager em, final Date now,
			final Sensor sensor) {

		final SensorValues sensorData = new SensorValues();

		sensorData.setSensorName(sensor.getSensorName());

		final String queryString = "select sd from SensorData sd where sd.sensor=:sensor and sd.dateTime>=:timeframe";

		final Date twoDaysAgo = new Date((new Date()).getTime() - (86400 * 1000));

		final List<SensorData> data = em.createQuery(queryString, SensorData.class).setParameter("sensor", sensor)
				.setParameter("timeframe", twoDaysAgo).getResultList();

		String latestValue = "";
		SensorValue lastSensorValue = null;

		// filter pressure by 0.2 percent changes only
		if ("PRESSURE".equals(sensor.getSensorType())) {
			for (final SensorData sData : data) {
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
		} else {
			for (final SensorData sData : data) {
				if ((lastSensorValue == null) || !(lastSensorValue.getValue().equals(sData.getValue()))) {
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

		// add a last value for the charts
		final SensorValue latestInterpolatedValue = new SensorValue();
		latestInterpolatedValue.setDateTime(now);
		latestInterpolatedValue.setValue(latestValue);
		sensorData.getValues().add(latestInterpolatedValue);

		sensorDatas.getSensorData().add(sensorData);

	}

	/**
	 * @param existingSensorData
	 * @param requestSensorData
	 * @param isNumeric
	 * @param valueAsDouble
	 * @return
	 */
	public boolean mergeExistingData(SensorData existingSensorData, final SensorData requestSensorData,
			boolean isNumeric) {

		boolean mergeExisting = false;
		if ((existingSensorData != null)) {

			if (existingSensorData.getValue().equals(requestSensorData.getValue())) {
				mergeExisting = true;
			} else {
				String sensorValue = requestSensorData.getValue().replace(",", ".");
				boolean secondNumericCheck = NumberUtils.isCreatable(sensorValue);
				if (isNumeric && secondNumericCheck) {
					final double valueAsDouble = Double.parseDouble(sensorValue);
					final double deadbandPercent = existingSensorData.getSensor().getDeadbandPercent();

					String existingValue = existingSensorData.getValue().replace(",", ".");

					if (NumberUtils.isCreatable(existingValue)) {

						final double existingValueAsDouble = Double.parseDouble(existingValue);

						final double difference = existingValueAsDouble * (deadbandPercent / 1000);

						final double lowerLimit = existingValueAsDouble - difference;
						final double higherLimit = existingValueAsDouble + difference;

						if ((lowerLimit <= valueAsDouble) && (valueAsDouble <= higherLimit)) {
							mergeExisting = true;
						}
					}
				}

			}

		}
		return mergeExisting;
	}

	@POST
	@Path("rfsniffer")
	public void registerRFEvent(final RFEvent event) throws SensorDataLimitViolationException {

		final String code = Integer.toString(event.getCode());
		LOG.info("RF Event: " + code);

		try {
			final Switch sw = em
					.createQuery("select sw from Switch sw where sw.onCode=:code or sw.offCode=:code", Switch.class)
					.setParameter("code", code).getSingleResult();

			if (sw != null) {

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

				bus.publish("SwitchEvent", switchEvent);

				sw.setLatestStatus(status);
				sw.setLatestStatusFrom(new Date());

				em.persist(sw);

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
			LOG.error(e);
		}

	}

	@POST
	@Path("save")
	public GenericStatus save(final SensorDataRoomSaveRequest request) throws SensorDataLimitViolationException {

		if (request == null) {
			LOG.info("got null request");
			return new GenericStatus(false);
		}

		Long roomID = request.getRoomID();

		final String mac = request.getMac();
		if ((roomID == null) && (mac != null)) {

			roomID = deviceService.getRoomIdForMac(mac);
			if (roomID == null) {
				// mac given but no room found
				return new GenericStatus(false);
			}
		}

		LOG.info("Found roomId" + roomID);

		final List<Sensor> sensorList = em
				.createQuery("select s from Sensor s where s.room=(select r from Room r where r.id=:roomId)",
						Sensor.class)
				.setParameter("roomId", roomID).getResultList();

		// fix empty or wrong timestamps
		if ((request.getTimestamp() == null) || (request.getTimestamp().getTime() < (10000 * 1000))) {
			request.setTimestamp(new Date());
		}

		saveSensorData(request, roomID, sensorList);
		return new GenericStatus(true);
	}

	private void saveSensorData(final SensorDataRoomSaveRequest request, Long roomID, final List<Sensor> sensorList)
			throws SensorDataLimitViolationException {
		if (sensorList != null) {
			for (final Sensor sensor : sensorList) {

				saveSingleSensorData(request, sensor);
			}

		} else {
			LOG.info("found no sensors for room " + roomID);
		}
	}

	private void saveSingleSensorData(final SensorDataRoomSaveRequest request, final Sensor sensor)
			throws SensorDataLimitViolationException {
		if ("TEMPERATURE".equals(sensor.getSensorType())) {
			LOG.info("Saving temperature to sensor: " + sensor.getId());
			saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getTemperature()),
					request.getTimestamp());
		} else if ("HUMIDITY".equals(sensor.getSensorType())) {
			LOG.info("Saving humidity to sensor: " + sensor.getId());
			saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getHumidity()),
					request.getTimestamp());
		} else if ("PRESSURE".equals(sensor.getSensorType())) {
			saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getPressure()),
					request.getTimestamp());
		} else if ("VCC".equals(sensor.getSensorType())) {
			saveSensorDataWithTime(sensor.getId(), Float.toString(request.getData().getVcc()), request.getTimestamp());
		}
	}

	@GET
	@Path("forroom/save/{sensorId}/{value}")
	public boolean saveSensorData(@PathParam("sensorId") final Long sensorId, @PathParam("value") final String value)
			throws SensorDataLimitViolationException {
		return saveSensorDataWithTime(sensorId, value, new Date());
	}

	@POST
	@Path("forroom/save")
	@ConsumeEvent(value = "SensorDataSaveRequest", blocking = true)
	@Transactional
	public void saveSensorData(final SensorDataSaveRequest request) throws SensorDataLimitViolationException {
		System.out.println("save request");
		Sensor sensor = null;
		if (request.getSensorId() != null) {
			Long sensorId = request.getSensorId();
			if (sensorsList.containsKey(sensorId)) {
				sensor = sensorsList.get(sensorId);
				LOG.debug("got sensor from cache");
				System.out.println("got sensor from cache: " + sensor.getId());
			} else {
				LOG.debug("looking for sensorId: " + sensorId);
				System.out.println("looking for sensorId: " + sensorId);

				try {
				List<Sensor> sensors = em.createQuery("select s from Sensor s where s.id=:sensorId", Sensor.class)
						.setParameter("sensorId", sensorId).getResultList();

				if (sensors != null && !sensors.isEmpty()) {
					sensor=sensors.get(0);
					System.out.println("sensor: " + sensor);
					System.out.println("sensor: " + sensor.getSensorName());

					sensorsList.put(sensorId, sensor);
				}
				System.out.println("nothing found");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} else if (request.getSensorData() != null && request.getSensorData().getSensor() != null) {

			String sensorTechnicalType = request.getSensorData().getSensor().getSensorTechnicalType();
			List<Sensor> sensors = em
					.createQuery("select s from Sensor s where s.sensorTechnicalType=:sensorTechnicalType",
							Sensor.class)
					.setParameter("sensorTechnicalType", sensorTechnicalType).getResultList();

			if (sensors != null && !sensors.isEmpty()) {
				sensor = sensors.get(0);
			} else {
				LOG.debug("found no sensor for technical type: " + sensorTechnicalType);

				sensor = new Sensor();
				sensor.setSensorTechnicalType(sensorTechnicalType);
				sensor.setSensorName(sensorTechnicalType);
				sensor.setDeadbandPercent(0);

				em.persist(sensor);

				List<Sensor> sensorsNew = em
						.createQuery("select s from Sensor s where s.sensorTechnicalType=:sensorTechnicalType",
								Sensor.class)
						.setParameter("sensorTechnicalType", sensorTechnicalType).getResultList();
				if (sensorsNew != null && !sensorsNew.isEmpty()) {
					sensor = sensorsNew.get(0);

				} else {
					throw new NoResultException("No sensor found for technicalType: " + sensorTechnicalType);
				}
			}
		} else {
			System.out.println("sensor id not set");
			throw new NoResultException("sensor id not set and no sensor provided");
		}

		if (sensor != null) {
			LOG.debug("found a sensor. id: " + sensor.getId());
			System.out.println("found a sensor. id: " + sensor.getId());

			final SensorData sensorData;
			Long sensorId = sensor.getId();
			SensorData existingSensorData = null;

			if (sensorDataMap.containsKey(sensorId)) {
				existingSensorData = sensorDataMap.get(sensorId);
			} else {

				final List<SensorData> existingSensorDataList = em.createQuery(
						"select sd from SensorData sd where sd.sensor IN (select s from Sensor s where s.id=:sensorId) order by sd.dateTime desc",
						SensorData.class).setMaxResults(1).setParameter("sensorId", sensorId).getResultList();

				if (!existingSensorDataList.isEmpty()) {
					existingSensorData = existingSensorDataList.get(0);
				}
			}

			final SensorData requestSensorData = request.getSensorData();
			String currentValue = requestSensorData.getValue();

			boolean isNumeric = NumberUtils.isCreatable(currentValue.replace(",", "."));
			if (isNumeric) {
				final double valueAsDouble = Double.parseDouble(currentValue.replace(",", "."));
				final DecimalFormat df = new DecimalFormat("#.##");
				df.setRoundingMode(RoundingMode.CEILING);

				requestSensorData.setValue(df.format(valueAsDouble));

				// checking minimum value limit
				if (sensor.getMinValue() != null) {
					double minValue = Double.parseDouble(sensor.getMinValue());
					if (valueAsDouble < minValue) {
						LOG.debug("Sensor ID: " + sensor.getId() + " Name: " + sensor.getSensorName() + " Value: "
								+ valueAsDouble + " less than minimum: " + minValue);
						throw new SensorDataLimitViolationException();
					}
				}

				// checking maximum value limit
				if (sensor.getMaxValue() != null) {
					double maxValue = Double.parseDouble(sensor.getMaxValue());
					if (valueAsDouble > maxValue) {
						LOG.debug("Sensor ID: " + sensor.getId() + " Name: " + sensor.getSensorName() + " Value: "
								+ valueAsDouble + " more than maxmum: " + maxValue);
						throw new SensorDataLimitViolationException();
					}
				}
			} else {
				requestSensorData.setValue(currentValue);
			}

			final boolean mergeExisting = mergeExistingData(existingSensorData, requestSensorData, isNumeric);

			if (mergeExisting && existingSensorData != null) {
				LOG.debug("merging data");
				existingSensorData.setValidThru(new Date());
				em.merge(existingSensorData);

				LOG.debug("Committing data: " + existingSensorData.getValue());
			} else {
				if ((existingSensorData != null) && (requestSensorData.getDateTime() != null)) {
					existingSensorData.setValidThru(new Date(requestSensorData.getDateTime().getTime() - 1000));
					em.merge(existingSensorData);
				}

				LOG.debug("saving new data point");
				sensorData = requestSensorData;
				sensorData.setSensor(sensor);
				em.persist(sensorData);

				sensorDataMap.put(sensorId, sensorData);

				bus.publish("EventObject", new EventObject(sensorData));
				LOG.debug("Committing data: " + sensorData.getValue());
			}
		} else {
			LOG.error("sensor is null");
		}

	}

	private boolean saveSensorDataWithTime(final Long sensorId, final String value, final Date timestamp)
			throws SensorDataLimitViolationException {
		final SensorDataSaveRequest sensorDataSaveRequest = new SensorDataSaveRequest();
		sensorDataSaveRequest.setSensorId(sensorId);
		final SensorData sensorData = new SensorData();
		sensorData.setValue(value);
		sensorData.setDateTime(timestamp);
		sensorDataSaveRequest.setSensorData(sensorData);

		try {
			saveSensorData(sensorDataSaveRequest);
		} catch (SecurityException | IllegalStateException | SensorDataLimitViolationException e) {
			return false;
		}

		return true;
	}
}
