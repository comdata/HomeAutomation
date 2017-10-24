package cm.homeautomation.sensors.powermeter;

import java.math.BigDecimal;
import java.sql.Timestamp;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

import java.util.ArrayList;
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
import cm.homeautomation.entities.PowerIntervalData;
import cm.homeautomation.entities.PowerMeterPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.PowerMeterData;

/**
 * receiver power meter data and save it to the database
 * 
 * @author christoph
 *
 */
public class PowerMeterSensor {

	private RequestRateLimiter requestRateLimiter;

	public PowerMeterSensor() {
		Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(1, TimeUnit.MINUTES, 1)); // 1 per
																											// minute
		requestRateLimiter = new InMemorySlidingWindowRequestRateLimiter(rules);
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		requestRateLimiter = null;
		EventBusService.getEventBus().unregister(this);

	}

	public static void compress(String[] args) {
		if (args != null && args.length > 0) {

			int numberOfEntriesToCompress = Integer.parseInt(args[0]);

			EntityManager em = EntityManagerService.getNewManager();

			em.getTransaction().begin();

			@SuppressWarnings("unchecked")
			List<Object[]> rawResultList = em.createNativeQuery(
					"select sum(POWERCOUNTER) as counter, FROM_UNIXTIME(FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/("
							+ " 60 * 60))*60 *60) as TIMESLICE, MIN(TIMESTAMP), MAX(TIMESTAMP) "
							+ "from POWERMETERPING where COMPRESSED=0  and FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(60*60)) <= now() - INTERVAL 2 HOUR"
							+ " GROUP BY FLOOR(UNIX_TIMESTAMP(TIMESTAMP)/(" + "60"
							+ " * 60)) order by TIMESTAMP asc limit " + numberOfEntriesToCompress)
					.getResultList();

			for (Object[] resultElement : rawResultList) {
				PowerMeterPing compressPowerPing = new PowerMeterPing();
				compressPowerPing.setTimestamp((Timestamp) resultElement[1]);
				compressPowerPing.setCompressed(true);
				int powerCounter = ((BigDecimal) resultElement[0]).intValue();
				compressPowerPing.setPowerCounter(powerCounter);

				System.out.println();

				Timestamp minTimestamp = (Timestamp) resultElement[2];
				Timestamp maxTimestamp = (Timestamp) resultElement[3];

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

	/**
	 * handle power meter data from the MQTT receiver
	 * 
	 * @param eventObject
	 */
	@Subscribe
	@AllowConcurrentEvents
	public void handlePowerMeterData(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {

			try {
				EntityManager em = EntityManagerService.getNewManager();

				PowerMeterData powerData = (PowerMeterData) data;

				PowerMeterPing powerMeterPing = new PowerMeterPing();

				powerMeterPing.setTimestamp(new Date());
				powerMeterPing.setPowermeter(powerData.getPowermeter());

				em.getTransaction().begin();

				em.persist(powerMeterPing);
				em.getTransaction().commit();
				em.close();

			} catch (Exception e) {
				LogManager.getLogger(PowerMeterSensor.class).error("error persisting power data", e);
			}

			try {
				boolean parseBoolean = Boolean
						.parseBoolean(ConfigurationService.getConfigurationProperty("power", "sendSummaryData"));
				if (parseBoolean) {
					boolean overLimit = requestRateLimiter.overLimitWhenIncremented(PowerMeterData.class.getName());
					if (overLimit) {
						sendNewData();
					}
				}
			} catch (Exception e) {
				LogManager.getLogger(PowerMeterSensor.class).error("error creating and sending interval data", e);
			}

		}
	}

	private void sendNewData() {
		EntityManager em = EntityManagerService.getNewManager();

		BigDecimal oneMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;");

		BigDecimal oneMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 2 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		BigDecimal fiveMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;");

		BigDecimal fiveMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 6 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;");

		BigDecimal sixtyMinute = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;");

		BigDecimal sixtyMinuteTrend = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 61 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;;");

		BigDecimal today = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)=CURDATE() ;");

		BigDecimal yesterday = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)=date(now()- interval 1 day);");

		BigDecimal lastSevenDays = (BigDecimal) runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<=date(now()- interval 1 day);");

		BigDecimal lastEightDaysBeforeTillYesterday = runQeryForBigDecimal(em,
				"select sum(POWERCOUNTER)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<CURDATE();");

		PowerMeterIntervalData powerMeterIntervalData = new PowerMeterIntervalData();
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

		EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
		EventBusService.getEventBus().post(intervalEventObject);
	}

	private BigDecimal runQeryForBigDecimal(EntityManager em, String query) {
		Object queryResultObject = em.createNativeQuery(query).getSingleResult();

		BigDecimal result= (queryResultObject != null) ? ((BigDecimal) queryResultObject) : new BigDecimal(0);
		result=result.setScale(2, BigDecimal.ROUND_HALF_UP);
		return result;
	}

}
