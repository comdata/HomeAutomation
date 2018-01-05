package cm.homeautomation.zwave;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class ZWaveConnector {

	public static void main(String[] args) {
		new ZWaveConnector();
	}

	public ZWaveConnector() {
		init();
	}

	private void init() {
		final boolean enabled = Boolean.parseBoolean(ConfigurationService.getConfigurationProperty("zwave", "enabled"));
		if (enabled) {

			final HAZWaveEventListener eventListener = new HAZWaveEventListenerImpl();
			new ZWaveController(eventListener);
		}
	}
}
