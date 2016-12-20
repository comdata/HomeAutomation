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
	public void handleWindowBlindChange(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof PowerMeterData) {

			PowerMeterData powerData = (PowerMeterData) data;

			PowerMeterPing powerMeterPing = new PowerMeterPing();

			powerMeterPing.setTimestamp(new Date());
			powerMeterPing.setPowermeter(powerData.getPowermeter());

			em.getTransaction().begin();

			em.merge(powerMeterPing);
			em.getTransaction().commit();

			BigDecimal[] oneMinute = (BigDecimal[]) em
					.createNativeQuery(
							"select count(*)/1000*60 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 1 MINUTE;")
					.getSingleResult();
			BigDecimal[] fiveMinute = (BigDecimal[]) em
					.createNativeQuery(
							"select count(*)/1000*12 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 5 MINUTE;")
					.getSingleResult();
			BigDecimal[] sixtyMinute = (BigDecimal[]) em
					.createNativeQuery(
							"select count(*)/1000 from POWERMETERPING where TIMESTAMP >= now() - INTERVAL 60 MINUTE;")
					.getSingleResult();

			PowerMeterIntervalData powerMeterIntervalData = new PowerMeterIntervalData();
			powerMeterIntervalData.setOneMinute(oneMinute[0].floatValue());
			powerMeterIntervalData.setFiveMinute(fiveMinute[0].floatValue());
			powerMeterIntervalData.setSixtyMinute(sixtyMinute[0].floatValue());

			EventObject intervalEventObject = new EventObject(powerMeterIntervalData);
			EventBusService.getEventBus().post(intervalEventObject);

		}
	}

}
