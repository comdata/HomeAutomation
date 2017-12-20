package cm.homeautomation.zwave;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class ZWaveConnector {

	public ZWaveConnector() {
		init();
	}

	private void init() {
		final HAZWaveEventListener eventListener = new HAZWaveEventListenerImpl();
		new ZWaveController(eventListener);
	}
}
