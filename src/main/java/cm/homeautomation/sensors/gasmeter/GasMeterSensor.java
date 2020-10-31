package cm.homeautomation.sensors.gasmeter;

import java.util.Date;

import javax.persistence.EntityManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.GasMeterPing;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.GasmeterData;
import cm.homeautomation.services.base.AutoCreateInstance;

/**
 * receiver gas meter data and save it to the database
 *
 * @author christoph
 *
 */
@AutoCreateInstance
public class GasMeterSensor {

	private final EntityManager em;

	public GasMeterSensor() {
		em = EntityManagerService.getNewManager();
		EventBusService.getEventBus().register(this);
	}

	public void destroy() {
		EventBusService.getEventBus().unregister(this);

	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void handleGasMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof GasmeterData) {

			final GasmeterData gasData = (GasmeterData) data;

			final GasMeterPing gasMeterPing = new GasMeterPing();

			gasMeterPing.setTimestamp(new Date());
			gasMeterPing.setGasMeter(gasData.getGasMeter());

			em.getTransaction().begin();

			em.merge(gasMeterPing);
			em.getTransaction().commit();

		}
	}

}
