package cm.homeautomation.tradfri;

import cm.homeautomation.entities.Light;

public class LightChangedEvent {

	private final Light light;

	public LightChangedEvent(final Light light) {
		this.light = light;

	}

	public Light getLight() {
		return light;
	}

}
