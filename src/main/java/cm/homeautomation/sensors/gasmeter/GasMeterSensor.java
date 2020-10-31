package cm.homeautomation.sensors.gasmeter;

import java.util.Date;

import javax.inject.Singleton;
import javax.persistence.EntityManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.GasMeterPing;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.sensors.GasmeterData;
import io.quarkus.vertx.ConsumeEvent;

/**
 * receiver gas meter data and save it to the database
 *
 * @author christoph
 *
 */
@Singleton
public class GasMeterSensor {

	private final EntityManager em;

	public GasMeterSensor() {
		em = EntityManagerService.getNewManager();
	}

	@ConsumeEvent(value = "EventObject", blocking = true)
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
