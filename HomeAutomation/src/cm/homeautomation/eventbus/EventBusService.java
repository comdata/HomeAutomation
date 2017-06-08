package cm.homeautomation.eventbus;

public class EventBusService {
	private static CustomEventBus eventBus = null;

	public static CustomEventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new CustomEventBus();
		}
		return eventBus;
	}

}
