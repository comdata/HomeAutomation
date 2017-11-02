package cm.homeautomation.tradfri;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.light.LightService;

public class TradfriBulbListenerImpl implements TradfriBulbListener {

	private static final String TRADFRI = "TRADFRI";
	private EntityManager em;

	@Override
	public void bulb_state_changed(final LightBulb bulb) {
		LogManager.getLogger(this.getClass()).error("Bulb event registered");

		final Light light = LightService.getInstance().getLightForTypeAndExternalId(TRADFRI,
				Integer.toString(bulb.getId()));

		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		if (light instanceof DimmableLight) {
			final DimmableLight dimLight = (DimmableLight) light;
			final int intensity = bulb.getIntensity();

			dimLight.setBrightnessLevel(intensity);

			// set on or off
			dimLight.setPowerState((intensity == dimLight.getMinimumValue()) ? false : true);

		}

		EventBusService.getEventBus().post(new EventObject(new LightChangedEvent(light)));

		em.merge(light);

		em.getTransaction().commit();
		LogManager.getLogger(this.getClass()).error("Bulb event done");

	}

}
