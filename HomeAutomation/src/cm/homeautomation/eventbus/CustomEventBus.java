package cm.homeautomation.eventbus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class CustomEventBus extends AsyncEventBus {

	Map<String, Class> classes = new HashMap<String, Class>();

	EventBus eventBus;

	public CustomEventBus() {
		super(Executors.newCachedThreadPool());
		eventBus = this;
	}

	@Override
	public void register(Object object) {

		Class clazz = object.getClass();
		String clazzName = clazz.getName();

		if (classes.containsKey(clazzName)) {
			System.out.println("Class already registered on eventbus: " + clazzName);
		} else {
			System.out.println("Registering Class on eventbus: " + clazz.getName());
			eventBus.register(object);
			classes.put(clazzName, clazz);
		}
	}

	@Override
	public void unregister(Object object) {
		classes.remove(object.getClass());
		eventBus.unregister(object);
	}

}
