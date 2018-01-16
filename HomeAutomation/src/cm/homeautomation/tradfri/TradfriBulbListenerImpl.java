package cm.homeautomation.tradfri;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.light.LightService;

public class TradfriBulbListenerImpl implements TradfriBulbListener {

	private static final String TRADFRI = "TRADFRI";
	private EntityManager em;

	@Override
	public void bulb_state_changed(final LightBulb bulb) {
		LogManager.getLogger(this.getClass()).trace("Bulb event registered");

		final Light light = LightService.getInstance().getLightForTypeAndExternalId(TRADFRI,
				Integer.toString(bulb.getId()));

		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		if (light instanceof DimmableLight) {
			final DimmableLight dimLight = (DimmableLight) light;
			final int intensity = bulb.getIntensity();

			if (bulb.isOnline()) {
				dimLight.setBrightnessLevel(intensity);
			} else {
				dimLight.setBrightnessLevel(dimLight.getMinimumValue());
			}

		}

		if (light instanceof RGBLight) {
			final RGBLight rgbLight = (RGBLight) light;
			rgbLight.setColor(bulb.getColor());

		}

		if (bulb.isOnline()) {
			// set on or off
			light.setPowerState(bulb.isOn());
		} else {
			light.setPowerState(false);
		}

		EventBusService.getEventBus().post(new EventObject(new LightChangedEvent(light)));

		em.persist(light);

		em.getTransaction().commit();
		LogManager.getLogger(this.getClass()).trace("Bulb event done");

	}

}
