package cm.homeautomation.sensors.powermeter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.db.InfluxDBService;
import cm.homeautomation.entities.PowerMeterPing;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.PowerMeterData;
import cm.homeautomation.services.sensors.InfluxSensorData;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * receiver power meter data and save it to the database
 *
 * @author christoph
 *
 */
@Singleton
public class PowerMeterSensor {

	@Inject
	EventBus bus;

	@Inject
	InfluxDBService influxDBService;

	/**
	 * perform compression by aggregating the data for one hour blocks
	 * 
	 * @param args
	 */
	public static void compress(final String[] args) {
		if ((args != null) && (args.length > 0)) {

			final int numberOfEntriesToCompress = Integer.parseInt(args[0]);

			final EntityManager em = EntityManagerService.getNewManager();

			em.getTransaction().begin();

			@SuppressWarnings("unchecked")
			final List<Object[]> rawResultList = em.createNativeQuery(
					"select sum(POWERCOUNTER) as counter, FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/("
							+ " 60 * 60))*60 *60) as TIMESLICE, MIN(TIMESTAMP), MAX(TIMESTAMP) "
							+ "from POWERMETERPING where COMPRESSED=0  and (FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(60*60))*60*60) <= UNIX_TIMESTAMP(now() - INTERVAL 4 HOUR)"
							+ " GROUP BY FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(" + "60"
							+ " * 60)) order by TIMESTAMP asc limit " + numberOfEntriesToCompress,
					Object[].class).getResultList();

			for (final Object[] resultElement : rawResultList) {
				final PowerMeterPing compressPowerPing = new PowerMeterPing();
				compressPowerPing.setTimestamp((Timestamp) resultElement[1]);
				compressPowerPing.setCompressed(true);
				final int powerCounter = ((BigDecimal) resultElement[0]).intValue();
				compressPowerPing.setPowerCounter(powerCounter);

				final Timestamp minTimestamp = (Timestamp) resultElement[2];
				final Timestamp maxTimestamp = (Timestamp) resultElement[3];

				LogManager.getLogger(PowerMeterSensor.class).error("Min Timestamp: {} max timestamp: {} counter: {}",
						minTimestamp, maxTimestamp, powerCounter);

				em.createQuery(
						"delete from PowerMeterPing p where p.timestamp>=:minTimestamp and p.timestamp<=:maxTimestamp")
						.setParameter("minTimestamp", minTimestamp).setParameter("maxTimestamp", maxTimestamp)
						.executeUpdate();

				em.persist(compressPowerPing);
			}

			em.getTransaction().commit();

		} else {
			LogManager.getLogger(PowerMeterSensor.class)
					.error("arguments not specified correctly - expecting number of entries for first method call");
		}
	}

	private RequestRateLimiter requestRateLimiter;

	public PowerMeterSensor() {
		init();
	}

	private void init() {
		final Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(Duration.ofSeconds(60), 2)); // 1
																													// per
		// 2 minutes
		requestRateLimiter = new InMemorySlidingWindowRequestRateLimiter(rules);

	}

	public void destroy() {
		requestRateLimiter = null;

	}

	void startup(@Observes StartupEvent event) {
		init();
	}

	public void saveToInflux(PowerMeterPing powerMeterPing) {
		InfluxDBClient influxDBClient = influxDBService.getClient();

		try (WriteApi writeApi = influxDBClient.getWriteApi()) {

			InfluxSensorData influxSensorData = new InfluxSensorData();

			influxSensorData.setValue(Integer.toString(powerMeterPing.getPowerCounter()));
			influxSensorData.setSensorName("PowerMeterPing");

			influxSensorData.setDateTime(powerMeterPing.getTimestamp().toInstant());

			writeApi.writeMeasurement(WritePrecision.MS, influxSensorData);
		}
	}

	/**
	 * handle power meter data from the MQTT receiver
	 *
	 * @param eventObject
	 */
	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handlePowerMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {

			try {
				final EntityManager em = EntityManagerService.getManager();

				final PowerMeterData powerData = (PowerMeterData) data;

				final PowerMeterPing powerMeterPing = new PowerMeterPing();

				powerMeterPing.setPowermeter(powerData.getPowermeter());
				powerMeterPing.setPowerCounter(powerData.getPowermeter());
				powerMeterPing.setTimestamp(new Date());

				em.getTransaction().begin();

				em.persist(powerMeterPing);
				em.getTransaction().commit();

			} catch (final Exception e) {
				LogManager.getLogger(PowerMeterSensor.class).error("error persisting power data", e);
			}

			try {
				final boolean parseBoolean = Boolean
						.parseBoolean(ConfigurationService.getConfigurationProperty("power", "sendSummaryData"));
				if (parseBoolean) {
					final boolean overLimit = requestRateLimiter
							.overLimitWhenIncremented(PowerMeterData.class.getName());
					if (overLimit) {
						final Runnable sendNewDataService = new Runnable() {
							@Override
							public void run() {
								try {
									sendNewData();
								} catch (final Exception e) {
									LogManager.getLogger(this.getClass()).error("Failed sending new data", e);
								}
							}
						};
						new Thread(sendNewDataService).start();

					}
				}
			} catch (final Exception e) {
				LogManager.getLogger(PowerMeterSensor.class).error("error creating and sending interval data", e);
			}

		}
	}

	private BigDecimal runQueryForBigDecimal(final EntityManager em, final String query) {
		final Object queryResultObject = em.createNativeQuery(query).getSingleResult();

		BigDecimal result = (queryResultObject != null) ? ((BigDecimal) queryResultObject) : BigDecimal.ZERO;
		result = result.setScale(2, RoundingMode.HALF_UP);
		return result;
	}

	private void sendNewData() {
		final EntityManager em = EntityManagerService.getManager();

		final BigDecimal oneMinute = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;");

		final BigDecimal oneMinuteTrend = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 2 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		final BigDecimal fiveMinute = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;");

		final BigDecimal fiveMinuteTrend = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 6 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		final BigDecimal sixtyMinute = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;");

		final BigDecimal sixtyMinuteTrend = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 61 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;;");

		final BigDecimal today = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000 from POWERMETERPING where date(TIMESTAMP)=CURDATE() ;");

		final BigDecimal yesterday = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000 from POWERMETERPING where date(TIMESTAMP)=date(now()- interval 1 day);");

		final BigDecimal lastSevenDays = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/10000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<=date(now()- interval 1 day);");

		final BigDecimal lastEightDaysBeforeTillYesterday = runQueryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<CURDATE();");

		final PowerMeterIntervalData powerMeterIntervalData = new PowerMeterIntervalData();
		powerMeterIntervalData.setOneMinute(oneMinute.floatValue());
		powerMeterIntervalData.setOneMinuteTrend(oneMinute.compareTo(oneMinuteTrend));
		powerMeterIntervalData.setFiveMinute(fiveMinute.floatValue());
		powerMeterIntervalData.setFiveMinuteTrend(fiveMinute.compareTo(fiveMinuteTrend));
		powerMeterIntervalData.setSixtyMinute(sixtyMinute.floatValue());
		powerMeterIntervalData.setSixtyMinuteTrend(sixtyMinute.compareTo(sixtyMinuteTrend));
		powerMeterIntervalData.setToday(today.floatValue());
		powerMeterIntervalData.setYesterday(yesterday.floatValue());
		powerMeterIntervalData.setLastSevenDays(lastSevenDays.floatValue());
		powerMeterIntervalData.setLastSevenDaysTrend(lastSevenDays.compareTo(lastEightDaysBeforeTillYesterday));

		final EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
		bus.publish("EventObject", intervalEventObject);
	}
}
