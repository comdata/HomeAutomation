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
			init();
		}
		return eventBus;
	}

	public static void init() {
		eventBus = new CustomEventBus();
	}

}
