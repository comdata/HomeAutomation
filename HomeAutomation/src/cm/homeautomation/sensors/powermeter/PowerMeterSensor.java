package cm.homeautomation.sensors.powermeter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.PowerMeterPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.PowerMeterData;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

/**
 * receiver power meter data and save it to the database
 *
 * @author christoph
 *
 */
public class PowerMeterSensor {

	public static void compress(final String[] args) {
		if ((args != null) && (args.length > 0)) {

			final int numberOfEntriesToCompress = Integer.parseInt(args[0]);

			final EntityManager em = EntityManagerService.getNewManager();

			em.getTransaction().begin();

			@SuppressWarnings("unchecked")
			final List<Object[]> rawResultList = em.createNativeQuery(
					"select sum(POWERCOUNTER) as counter, FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/("
							+ " 60 * 60))*60 *60) as TIMESLICE, MIN(TIMESTAMP), MAX(TIMESTAMP) "
							+ "from POWERMETERPING where COMPRESSED=0  and FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(60*60)) <= UNIX_TIMESTAMP(now() - INTERVAL 4 HOUR)"
							+ " GROUP BY FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(" + "60"
							+ " * 60)) order by TIMESTAMP asc limit " + numberOfEntriesToCompress)
					.getResultList();

			for (final Object[] resultElement : rawResultList) {
				final PowerMeterPing compressPowerPing = new PowerMeterPing();
				compressPowerPing.setTimestamp((Timestamp) resultElement[1]);
				compressPowerPing.setCompressed(true);
				final int powerCounter = ((BigDecimal) resultElement[0]).intValue();
				compressPowerPing.setPowerCounter(powerCounter);

				System.out.println();

				final Timestamp minTimestamp = (Timestamp) resultElement[2];
				final Timestamp maxTimestamp = (Timestamp) resultElement[3];

				LogManager.getLogger(PowerMeterSensor.class).error("Min Timestamp: " + minTimestamp.toString()
						+ " max timestamp:" + maxTimestamp.toString() + " counter: " + powerCounter);

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
		final Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 1)); // 1 per
		// minute
		requestRateLimiter = new InMemorySlidingWindowRequestRateLimiter(rules);
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		requestRateLimiter = null;
		EventBusService.getEventBus().unregister(this);

	}

	/**
	 * handle power meter data from the MQTT receiver
	 *
	 * @param eventObject
	 */
	@Subscribe
	@AllowConcurrentEvents
	public void handlePowerMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {

			try {
				final EntityManager em = EntityManagerService.getNewManager();

				final PowerMeterData powerData = (PowerMeterData) data;

				final PowerMeterPing powerMeterPing = new PowerMeterPing();

				powerMeterPing.setTimestamp(new Date());
				powerMeterPing.setPowermeter(powerData.getPowermeter());

				em.getTransaction().begin();

				em.persist(powerMeterPing);
				em.getTransaction().commit();
				em.close();

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
						sendNewData();
					}
				}
			} catch (final Exception e) {
				LogManager.getLogger(PowerMeterSensor.class).error("error creating and sending interval data", e);
			}

		}
	}

	private BigDecimal runQeryForBigDecimal(final EntityManager em, final String query) {
		final Object queryResultObject = em.createNativeQuery(query).getSingleResult();

		BigDecimal result = (queryResultObject != null) ? ((BigDecimal) queryResultObject) : new BigDecimal(0);
		result = result.setScale(2, BigDecimal.ROUND_HALF_UP);
		return result;
	}

	private void sendNewData() {
		final EntityManager em = EntityManagerService.getNewManager();

		final BigDecimal oneMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;");

		final BigDecimal oneMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 2 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		final BigDecimal fiveMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;");

		final BigDecimal fiveMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 6 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		final BigDecimal sixtyMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;");

		final BigDecimal sixtyMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 61 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;;");

		final BigDecimal today = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)=CURDATE() ;");

		final BigDecimal yesterday = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)=date(now()- interval 1 day);");

		final BigDecimal lastSevenDays = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<=date(now()- interval 1 day);");

		final BigDecimal lastEightDaysBeforeTillYesterday = runQeryForBigDecimal(em,
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

		em.clear();
		em.close();

		final EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
		EventBusService.getEventBus().post(intervalEventObject);
	}

}
