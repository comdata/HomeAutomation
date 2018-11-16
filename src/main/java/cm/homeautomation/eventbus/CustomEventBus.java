package cm.homeautomation.eventbus;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

public class CustomEventBus {

	private EventBus eventBus = null;

	private final Map<String, Class> classes = new HashMap<>();

	public CustomEventBus() {
		if (eventBus == null) {
			eventBus = EventBus.getDefault();
		}
	}
	
	public static EventBus getEventBus() {
		return EventBus.getDefault();
	}

	public Map<String, Class> getClasses() {
		return classes;
	}

	public void post(final Object event) {
		eventBus.post(event);
	}

	public void register(final Object object) {

		final Class clazz = object.getClass();
		final String clazzName = clazz.getName();

		if (getClasses().containsKey(clazzName)) {
			LogManager.getLogger(this.getClass()).debug("Class already registered on eventbus: " + clazzName);
		} else {
			LogManager.getLogger(this.getClass()).debug("Registering Class on eventbus: " + clazz.getName());
			try {
				eventBus.register(object);
			} catch (final EventBusException e) {

			}
			getClasses().put(clazzName, clazz);
		}
	}

	public void unregister(final Object object) {
		eventBus.unregister(object);
		getClasses().remove(object.getClass().getName());

	}
}
