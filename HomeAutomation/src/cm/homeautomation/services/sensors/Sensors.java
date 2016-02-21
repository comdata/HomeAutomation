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

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.entities.SensorData;
import cm.homeautomation.entities.Switch;
import cm.homeautomation.hap.HAPService;
import cm.homeautomation.hap.HAPTemperatureSensor;
import cm.homeautomation.sensors.RFEvent;
import cm.homeautomation.sensors.SensorDataSaveRequest;
import cm.homeautomation.sensors.SensorDatas;
import cm.homeautomation.sensors.SensorValue;
import cm.homeautomation.sensors.SensorValues;
import cm.homeautomation.services.actor.ActorEndpoint;
import cm.homeautomation.services.actor.ActorEndpointConfigurator;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.overview.OverviewEndPointConfiguration;
import cm.homeautomation.services.overview.OverviewService;
import cm.homeautomation.services.overview.OverviewTile;
import cm.homeautomation.services.overview.OverviewWebSocket;

@Path("sensors")
public class Sensors extends BaseService {

	private ActorEndpointConfigurator actorEndpointConfigurator;
	private ActorEndpoint endpointInstance;
	private OverviewEndPointConfiguration overviewEndPointConfiguration;
	private OverviewWebSocket overviewEndpoint;

	@POST
	@Path("rfsniffer")
	public void registerRFEvent(RFEvent event) {
		String code = Integer.toString(event.getCode());
		System.out.println("RF Event: " + code);
		EntityManager em = EntityManagerService.getNewManager();

		if (actorEndpointConfigurator == null) {
			actorEndpointConfigurator = new ActorEndpointConfigurator();
			try {
				endpointInstance = actorEndpointConfigurator.getEndpointInstance(ActorEndpoint.class);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
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

				endpointInstance.handleEvent(Long.toString(sw.getId()), status);
				// endpointInstance.onMessage("Switch: "++" new status:
				// "+status, null);

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
		} catch (NoResultException e) {

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
			double valueAsDouble = Double.parseDouble(requestSensorData.getValue().replace(",", "."));
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);

			requestSensorData.setValue(df.format(valueAsDouble).toString());
			if (existingSensorData != null && existingSensorData.getValue().equals(requestSensorData.getValue())) {
				existingSensorData.setValidThru(new Date());
				sensorData = em.merge(existingSensorData);
			} else {
				if (existingSensorData != null && requestSensorData != null
						&& requestSensorData.getDateTime() != null) {
					existingSensorData.setValidThru(new Date(requestSensorData.getDateTime().getTime() - 1000));
					sensorData = em.merge(existingSensorData);
				}

				sensorData = requestSensorData;
				sensorData.setSensor(sensor);
				em.persist(sensorData);
			}

			em.getTransaction().commit();

			if ("TEMPERATURE".equals(sensorData.getSensor().getSensorType())) {

				HAPTemperatureSensor hapTemperatureSensor = HAPService.getInstance().getTemperatureSensors()
						.get(sensorData.getSensor().getId());

				if (hapTemperatureSensor != null) {
					hapTemperatureSensor.setTemperature(new Double(valueAsDouble));
					HomekitCharacteristicChangeCallback subscribeCallback = hapTemperatureSensor.getSubscribeCallback();
					if (subscribeCallback != null) {
						subscribeCallback.changed();
					}
				}
			}

			OverviewTile overviewTileForRoom = new OverviewService()
					.getOverviewTileForRoom(sensorData.getSensor().getRoom());
			try {

				if (overviewEndPointConfiguration == null) {
					overviewEndPointConfiguration = new OverviewEndPointConfiguration();
					overviewEndpoint = overviewEndPointConfiguration.getEndpointInstance(OverviewWebSocket.class);
				}

				overviewEndpoint.sendTile(overviewTileForRoom);

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("Not a sensor");
		}
	}

	@Path("forroom/{room}")
	@GET
	public SensorDatas getDataForRoom(@PathParam("room") String room) {
		SensorDatas sensorDatas = new SensorDatas();

		EntityManager em = EntityManagerService.getNewManager();
		em.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Object> sensors = em
				.createQuery("select s FROM Sensor s where s.room=(select r from Room r where r.id=:room)")
				.setParameter("room", Long.parseLong(room)).getResultList();

		Date now = new Date();

		final CountDownLatch latch = new CountDownLatch(sensors.size());

		for (Object object : sensors) {
			DataLoadThread dataLoadThread = new DataLoadThread(sensorDatas, em, now, object, latch);
			dataLoadThread.start();
		}

		try {
			latch.await();
			em.getTransaction().commit();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sensorDatas;
	}

	public void loadSensorData(SensorDatas sensorDatas, EntityManager em, Date now, Object object) {
		if (object instanceof Sensor) {
			Sensor sensor = (Sensor) object;

			EntityManager emSensor = EntityManagerService.getNewManager();

			emSensor.getTransaction().begin();

			SensorValues sensorData = new SensorValues();

			sensorData.setSensorName(sensor.getSensorName());

			String queryString = "select sd from SensorData sd where sd.sensor=:sensor and sd.dateTime>=:timeframe";

			Date twoDaysAgo = new Date((new Date()).getTime() - (86400 * 1000));
			@SuppressWarnings("unchecked")
			List<Object> data = emSensor.createQuery(queryString).setParameter("sensor", sensor)
					.setParameter("timeframe", twoDaysAgo).getResultList();

			String latestValue = "";
			SensorValue lastSensorValue = null;

			// filter pressure by 0.2 percent changes only
			if ("PRESSURE".equals(sensor.getSensorType())) {
				for (Object dataObject : data) {
					if (dataObject instanceof SensorData) {
						SensorData sData = (SensorData) dataObject;

						String currentValue = sData.getValue().replace(",", ".");
						String lastValue = "0";

						if (lastSensorValue != null) {
							lastValue = lastSensorValue.getValue().replace(",", ".");
						}

						double floatCurrentValue = Double.parseDouble(currentValue);

						double floatLastValue = Double.parseDouble(lastValue);

						if (floatLastValue != floatCurrentValue) {
							double diff = floatLastValue - floatCurrentValue;
							double diffAbsolute = Math.sqrt(diff * diff);

							if (diffAbsolute >= 1) {

								SensorValue sensorValue = new SensorValue();
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
				for (Object dataObject : data) {
					if (dataObject instanceof SensorData) {
						SensorData sData = (SensorData) dataObject;

						if (lastSensorValue==null || (lastSensorValue!=null && sData.getValue() != lastSensorValue.getValue())) {
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
				}
			}

			// add a last value for the charts
			SensorValue latestInterpolatedValue = new SensorValue();
			latestInterpolatedValue.setDateTime(now);
			latestInterpolatedValue.setValue(latestValue);
			sensorData.getValues().add(latestInterpolatedValue);

			sensorDatas.getSensorData().add(sensorData);

			emSensor.getTransaction().commit();
		}
	}

	class DataLoadThread extends Thread {
		private SensorDatas sensorDatas;
		private EntityManager em;
		private Date now;
		private Object object;
		private CountDownLatch latch;

		public DataLoadThread(SensorDatas sensorDatas, EntityManager em, Date now, Object object,
				CountDownLatch latch) {
			this.sensorDatas = sensorDatas;
			this.em = em;
			this.now = now;
			this.object = object;
			this.latch = latch;

		}

		public void run() {
			loadSensorData(sensorDatas, em, now, object);
			this.latch.countDown();
		}
	}
}
