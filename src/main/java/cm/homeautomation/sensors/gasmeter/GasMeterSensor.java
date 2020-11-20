package cm.homeautomation.sensors.gasmeter;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import cm.homeautomation.configuration.ConfigurationService;
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
@ApplicationScoped
public class GasMeterSensor {

	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;

	public GasMeterSensor() {

	}

	@ConsumeEvent(value = "EventObject", blocking = true)
	
	public void handleGasMeterData(final EventObject eventObject) {

		final Object data = eventObject.getData();
		if (data instanceof GasmeterData) {

			final GasmeterData gasData = (GasmeterData) data;

			final GasMeterPing gasMeterPing = new GasMeterPing();

			gasMeterPing.setTimestamp(new Date());
			gasMeterPing.setGasMeter(gasData.getGasMeter());

			

			em.merge(gasMeterPing);


		}
	}

}
