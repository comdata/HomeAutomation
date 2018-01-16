package cm.homeautomation.tradfri;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.DimmableLight;
import cm.homeautomation.entities.Light;
import cm.homeautomation.entities.RGBLight;
import cm.homeautomation.services.light.LightService;

public class TradfriGatewaylistenerImpl implements TradfriGatewayListener {
	private static final String TRADFRI = "TRADFRI";
	private EntityManager em;
	private final TradfriBulbListener bulbListener = new TradfriBulbListenerImpl();

	@Override
	public void bulb_discovered(final LightBulb b) {

		b.addLightBulbListner(bulbListener);

		em = EntityManagerService.getNewManager();
		em.getTransaction().begin();

		LogManager.getLogger(this.getClass()).trace("Bulb discovered: " + b.toString());

		Light light = LightService.getInstance().getLightForTypeAndExternalId(TRADFRI, Integer.toString(b.getId()));

		if (light == null) {

			if (b.isColorLight()) {
				light = new RGBLight();
			} else {
				light = new DimmableLight();
			}

			light.setName(b.getName());
			light.setExternalId(Integer.toString(b.getId()));
			light.setLightType(TRADFRI);
			light.setDateInstalled(b.getDateInstalled());
			light.setDateLastSeen(b.getDateLastSeen());
			light.setFirmware(b.getFirmware());
			light.setOnline(b.isOnline());
			light.setPowerState(b.isOn());
			if (light instanceof DimmableLight) {
				((DimmableLight) light).setMaximumValue(254);
			}

			em.persist(light);

		} else {
			if ((b.getName() != null) && !"".equals(b.getName())) {
				light.setName(b.getName());
			}
			light.setExternalId(Integer.toString(b.getId()));
			light.setLightType(TRADFRI);
			light.setDateInstalled(b.getDateInstalled());
			light.setDateLastSeen(b.getDateLastSeen());
			if (light instanceof DimmableLight) {
				((DimmableLight) light).setMaximumValue(254);
			}
			light.setFirmware(b.getFirmware());
			light.setOnline(b.isOnline());
			light.setPowerState(b.isOn());
			if (b.isColorLight() && (light instanceof RGBLight)) {
				((RGBLight) light).setColor(b.getColor());
			}

			em.persist(light);
		}
		em.getTransaction().commit();
	}

	@Override
	public void bulb_discovery_completed() {
		LogManager.getLogger(this.getClass()).trace("Bulb discovery complete");

	}

	@Override
	public void bulb_discovery_started(final int total_devices) {
		LogManager.getLogger(this.getClass()).trace("Bulb discovery started. Total devices: " + total_devices);

	}

	@Override
	public void gateway_initializing() {
		LogManager.getLogger(this.getClass()).trace("Gateway initializing");

	}

	@Override
	public void gateway_started() {
		LogManager.getLogger(this.getClass()).trace("Gateway started");

	}

	@Override
	public void gateway_stoped() {
		LogManager.getLogger(this.getClass()).trace("Gateway stopped");

	}

	@Override
	public void polling_completed(final int bulb_count, final int total_time) {
		LogManager.getLogger(this.getClass()).trace("Gateway polling completed");

	}

	@Override
	public void polling_started() {
		LogManager.getLogger(this.getClass()).trace("Gateway polling started");
	}

}
