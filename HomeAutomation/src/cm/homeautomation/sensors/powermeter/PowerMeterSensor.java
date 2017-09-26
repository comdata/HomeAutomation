package cm.homeautomation.sensors.powermeter;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
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

	public PowerMeterSensor() {
		
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	@AllowConcurrentEvents
	public void handlePowerMeterData(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {
			EntityManager em = EntityManagerService.getNewManager();
			
			PowerMeterData powerData = (PowerMeterData) data;

			PowerMeterPing powerMeterPing = new PowerMeterPing();

			powerMeterPing.setTimestamp(new Date());
			powerMeterPing.setPowermeter(powerData.getPowermeter());

			em.getTransaction().begin();

			em.persist(powerMeterPing);
			em.getTransaction().commit();
			em.close();
			
			em = EntityManagerService.getNewManager();

			BigDecimal oneMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;")
					.getSingleResult();
			
			BigDecimal oneMinuteTrend = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 2 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;")
					.getSingleResult();
			
			BigDecimal fiveMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;")
					.getSingleResult();
			
			BigDecimal fiveMinuteTrend = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 6 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;")
					.getSingleResult();
			
			BigDecimal sixtyMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;")
					.getSingleResult();
			
			BigDecimal sixtyMinuteTrend = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 61 MINUTE and TIMESTAMP <= now() - INTERVAL 1 MINUTE;;")
					.getSingleResult();

			BigDecimal today = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)=CURDATE() ;")
					.getSingleResult();

			
			BigDecimal yesterday = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)=date(now()- interval 1 day);")
					.getSingleResult();

			BigDecimal lastSevenDays = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<=date(now()- interval 1 day);")
					.getSingleResult();
			
			BigDecimal lastEightDaysBeforeTillYesterday = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)>=date(now()- interval 8 day) and date(TIMESTAMP)<CURDATE();")
					.getSingleResult();
			
			oneMinute=oneMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			oneMinuteTrend=oneMinuteTrend.setScale(2, BigDecimal.ROUND_HALF_UP);
			fiveMinute=fiveMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			fiveMinuteTrend=fiveMinuteTrend.setScale(2, BigDecimal.ROUND_HALF_UP);
			sixtyMinute=sixtyMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			sixtyMinuteTrend=sixtyMinuteTrend.setScale(2, BigDecimal.ROUND_HALF_UP);
			today=today.setScale(2, BigDecimal.ROUND_HALF_UP);
			yesterday=yesterday.setScale(2, BigDecimal.ROUND_HALF_UP);
			lastSevenDays=lastSevenDays.setScale(2, BigDecimal.ROUND_HALF_UP);
			lastEightDaysBeforeTillYesterday=lastEightDaysBeforeTillYesterday.setScale(2, BigDecimal.ROUND_HALF_UP);
			
			
			
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
			
			em.close();
			
			EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
			EventBusService.getEventBus().post(intervalEventObject);

		}
	}

}
