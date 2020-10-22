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

	public CustomEventBus() {
		// do nothing
	}

	public static EventBus getEventBus() {
		return EventBus.getDefault();
	}

	public Map<String, Class<? extends Object>> getClasses() {
		return classes;
	}

	public void post(final Object event) {
		Runnable eventThread = () -> EventBus.getDefault().post(event);

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
				EventBus.getDefault().register(object);
			} catch (final EventBusException e) {
//				LogManager.getLogger(this.getClass()).error(e);
			}
			getClasses().put(clazzName, clazz);
		}
	}

	public void unregister(final Object object) {
		EventBus.getDefault().unregister(object);
		getClasses().remove(object.getClass().getName());

	}
}
