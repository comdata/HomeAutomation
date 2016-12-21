package cm.homeautomation.sensors.powermeter;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;

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

	private EntityManager em;

	public PowerMeterSensor() {
		em = EntityManagerService.getNewManager();
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	public void handlePowerMeterData(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {

			PowerMeterData powerData = (PowerMeterData) data;

			PowerMeterPing powerMeterPing = new PowerMeterPing();

			powerMeterPing.setTimestamp(new Date());
			powerMeterPing.setPowermeter(powerData.getPowermeter());

			em.getTransaction().begin();

			em.merge(powerMeterPing);
			em.getTransaction().commit();

			BigDecimal oneMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;")
					.getSingleResult();
			BigDecimal fiveMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;")
					.getSingleResult();
			BigDecimal sixtyMinute = (BigDecimal) em
					.createNativeQuery(
							"select count(*)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;")
					.getSingleResult();

			BigDecimal yesterday = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)=CURDATE() - 1;")
					.getSingleResult();

			BigDecimal lastSevenDays = (BigDecimal) em
					.createNativeQuery("select count(*)/1000 from POWERMETERPING where date(TIMESTAMP)>=CURDATE() - 7;")
					.getSingleResult();
			
			oneMinute=oneMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			fiveMinute=fiveMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			sixtyMinute=sixtyMinute.setScale(2, BigDecimal.ROUND_HALF_UP);
			yesterday=yesterday.setScale(2, BigDecimal.ROUND_HALF_UP);
			lastSevenDays=lastSevenDays.setScale(2, BigDecimal.ROUND_HALF_UP);

			PowerMeterIntervalData powerMeterIntervalData = new PowerMeterIntervalData();
			powerMeterIntervalData.setOneMinute(oneMinute.floatValue());
			powerMeterIntervalData.setFiveMinute(fiveMinute.floatValue());
			powerMeterIntervalData.setSixtyMinute(sixtyMinute.floatValue());
			powerMeterIntervalData.setYesterday(yesterday.floatValue());
			powerMeterIntervalData.setLastSevenDays(lastSevenDays.floatValue());

			
			
			EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
			EventBusService.getEventBus().post(intervalEventObject);

		}
	}

}
