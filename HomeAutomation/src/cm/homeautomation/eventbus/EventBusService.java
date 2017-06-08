package cm.homeautomation.eventbus;

import com.google.common.eventbus.EventBus;

public class EventBusService {
	private static CustomEventBus eventBus = null;

	public static CustomEventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new CustomEventBus();
		}
		return eventBus;
	}

}
