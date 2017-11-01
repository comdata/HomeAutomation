package cm.homeautomation.tradfri;

import javax.persistence.EntityManager;

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
		final Light light = LightService.getInstance().getLightForTypeAndExternalId(TRADFRI,
				Integer.toString(bulb.getId()));

		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		if (light instanceof DimmableLight) {
			final DimmableLight dimLight = (DimmableLight) light;
			dimLight.setBrightnessLevel(bulb.getIntensity());
		}

		EventBusService.getEventBus().post(new EventObject(new LightChangedEvent(light)));

		em.merge(light);

		em.getTransaction().commit();

	}

}
