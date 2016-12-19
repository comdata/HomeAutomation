package cm.homeautomation.sensors.powermeter;

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
		
			PowerMeterData powerData=(PowerMeterData)data;
			
			PowerMeterPing powerMeterPing = new PowerMeterPing();
			
			powerMeterPing.setTimestamp(new Date());
			powerMeterPing.setPowermeter(powerData.getPowermeter());
			
			em.getTransaction().begin();
			
			em.merge(powerMeterPing);
			em.getTransaction().commit();
			
		}
	}
	
}
