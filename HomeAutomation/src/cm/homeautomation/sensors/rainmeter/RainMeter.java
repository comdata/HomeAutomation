package cm.homeautomation.sensors.rainmeter;

import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.RainPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.RainData;

public class RainMeter {
	
	public RainMeter() {

		EventBusService.getEventBus().register(this);
	}
	
	@Subscribe
	public void handlePowerMeterData(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof RainData) {
			RainData rainData=(RainData)data;
			
			EntityManager em = EntityManagerService.getNewManager();
			em.getTransaction().begin();
			
			RainPing rainPing=new RainPing();
			rainPing.setMac(rainData.getMac());
			rainPing.setState(rainData.getState());
			rainPing.setState(rainData.getState());
			rainPing.setTimestamp(new Date());
			em.persist(rainPing);
			
			em.getTransaction().commit();
			
		}
	}

}
