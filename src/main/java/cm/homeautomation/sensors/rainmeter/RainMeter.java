package cm.homeautomation.sensors.rainmeter;

import java.util.Date;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.RainPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.RainData;
import io.quarkus.vertx.ConsumeEvent;

public class RainMeter {

	public RainMeter() {

		EventBusService.getEventBus().register(this);
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handlePowerMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof RainData) {
			final RainData rainData = (RainData) data;

			final EntityManager em = EntityManagerService.getNewManager();
			em.getTransaction().begin();

			final RainPing rainPing = new RainPing();
			rainPing.setMac(rainData.getMac());
			rainPing.setState(rainData.getState());
			rainPing.setState(rainData.getState());
			rainPing.setRainCounter(rainData.getRc());
			rainPing.setTimestamp(new Date());
			em.persist(rainPing);

			em.getTransaction().commit();

		}
	}

}
