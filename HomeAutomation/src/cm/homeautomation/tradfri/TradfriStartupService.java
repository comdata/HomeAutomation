package cm.homeautomation.tradfri;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class TradfriStartupService {

	private static TradfriStartupService instance;

	public static TradfriStartupService getInstance() {
		return instance;
	}

	public static void setInstance(final TradfriStartupService instance) {
		TradfriStartupService.instance = instance;
	}

	private TradfriGateway gw;

	public TradfriStartupService() {
		init();
		instance = this;

	}

	public void dimBulb(final String id, final int dimValue) {

		for (final LightBulb b : gw.bulbs) {
			if (Integer.toString(b.getId()).equals(id)) {
				b.setIntensity(dimValue);
			}

		}

	}

	private void init() {

		gw = new TradfriGateway(ConfigurationService.getConfigurationProperty("tradfri", "gateway"),
				ConfigurationService.getConfigurationProperty("tradfri", "secret"));
		final TradfriGatewayListener gatewayListener = new TradfriGatewaylistenerImpl();
		gw.addTradfriGatewayListener(gatewayListener);

		gw.initCoap();
		gw.dicoverBulbs();

		gw.startTradfriGateway();
	}
}
