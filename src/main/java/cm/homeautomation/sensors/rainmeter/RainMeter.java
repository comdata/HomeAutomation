package cm.homeautomation.sensors.rainmeter;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.RainPing;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.RainData;
import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class RainMeter {
	@Inject
	EntityManager em;

	@Inject
	ConfigurationService configurationService;

	@ConsumeEvent(value = "EventObject", blocking = true)
	public void handlePowerMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof RainData) {
			final RainData rainData = (RainData) data;

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
