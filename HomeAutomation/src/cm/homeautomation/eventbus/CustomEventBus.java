package cm.homeautomation.eventbus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class CustomEventBus {

	Map<String, Class> classes = new HashMap<String, Class>();

	static EventBus eventBus=null;

	public CustomEventBus() {
		if (eventBus==null) {
			eventBus=new AsyncEventBus(Executors.newCachedThreadPool());
		}
	}

	
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

	public void unregister(Object object) {
		classes.remove(object.getClass());
		eventBus.unregister(object);
	}
	
	 public void post(Object event) {
		 eventBus.post(event);
	 }

}
