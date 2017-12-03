package cm.homeautomation.tradfri;

import org.apache.logging.log4j.LogManager;

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
		instance = this;
		init();

	}

	public void dimBulb(final String id, final int dimValue) {

		for (final LightBulb b : gw.bulbs) {
			if (Integer.toString(b.getId()).equals(id)) {
				b.setIntensity(dimValue);
			}

		}

	}
	
	public void setColor(final String id, final String color) {

		for (final LightBulb b : gw.bulbs) {
			if (Integer.toString(b.getId()).equals(id)) {
				b.setColor(color);
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

		final Runnable tradfriRunner = new Runnable() {
			@Override
			public void run() {
				try {
					gw.startTradfriGateway();
				} catch (final Exception e) {
					e.printStackTrace();
					LogManager.getLogger(this.getClass()).info("Failed creating class");
				}
			}
		};
		new Thread(tradfriRunner).start();

	}
}
