package cm.homeautomation.eventbus;

import com.google.common.eventbus.EventBus;

public class EventBusService {
	private static EventBus eventBus = null;

	public static EventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new CustomEventBus();
		}
		return eventBus;
	}

}
