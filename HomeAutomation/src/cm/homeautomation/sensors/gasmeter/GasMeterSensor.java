package cm.homeautomation.sensors.gasmeter;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.GasMeterPing;
import cm.homeautomation.entities.PowerMeterPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.GasmeterData;
import cm.homeautomation.sensors.PowerMeterData;

/**
 * receiver gas meter data and save it to the database
 * 
 * @author christoph
 *
 */
public class GasMeterSensor {

	private EntityManager em;

	public GasMeterSensor() {
		em = EntityManagerService.getNewManager();
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleGasMeterData(EventObject eventObject) {

		Object data = eventObject.getData();
		if (data instanceof GasmeterData) {

			GasmeterData gasData = (GasmeterData) data;

			GasMeterPing gasMeterPing = new GasMeterPing();

			gasMeterPing.setTimestamp(new Date());
			gasMeterPing.setGasMeter(gasData.getGasMeter());

			em.getTransaction().begin();

			em.merge(gasMeterPing);
			em.getTransaction().commit();

		}
	}

}
