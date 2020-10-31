package cm.homeautomation.eventbus;

import java.util.HashMap;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

/**
 * Custom Eventbus object
 * 
 * @author cmertins
 *
 */
public class CustomEventBus {

	private static final Map<String, Class<? extends Object>> classes = new HashMap<>();
	private static EventBus eventBus;

	public CustomEventBus() {
		// do nothing
	}

	public static EventBus getEventBus() {
		if (eventBus == null) {
			eventBus = EventBus.getDefault();
		}
		return eventBus;
	}

	public Map<String, Class<? extends Object>> getClasses() {
		return classes;
	}

	public void post(final Object event) {
		Runnable eventThread = () -> getEventBus().post(event);

		new Thread(eventThread).start();
	}

	public void register(final Object object) {

		final Class<? extends Object> clazz = object.getClass();
		final String clazzName = clazz.getName();

		if (getClasses().containsKey(clazzName)) {
//			LogManager.getLogger(this.getClass()).debug("Class already registered on eventbus: {}", clazzName);
		} else {
//			LogManager.getLogger(this.getClass()).debug("Registering Class on eventbus: {}", clazz.getName());
			try {
				getEventBus().register(object);
			} catch (final EventBusException e) {
//				LogManager.getLogger(this.getClass()).error(e);
			}
			getClasses().put(clazzName, clazz);
		}
	}

	public void unregister(final Object object) {
		getEventBus().unregister(object);
		getClasses().remove(object.getClass().getName());

	}
}
