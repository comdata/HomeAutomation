package cm.homeautomation.eventbus;

import java.util.HashMap;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

public class CustomEventBus {

	static EventBus eventBus = null;

	private final Map<String, Class> classes = new HashMap<>();

	public CustomEventBus() {
		if (eventBus == null) {
			// eventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));

			eventBus = EventBus.getDefault();
		}
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
			System.out.println("Class already registered on eventbus: " + clazzName);
		} else {
			System.out.println("Registering Class on eventbus: " + clazz.getName());
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