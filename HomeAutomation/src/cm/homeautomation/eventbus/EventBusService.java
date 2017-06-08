package cm.homeautomation.eventbus;

/**
 * Event Bus service
 * 
 * @author cmertins
 *
 */
public class EventBusService {
	private static CustomEventBus eventBus = null;

	public static CustomEventBus getEventBus() {
		if (eventBus == null) {
			eventBus = new CustomEventBus();
		}
		return eventBus;
	}

}
