package cm.homeautomation.eventbus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class CustomEventBus extends EventBus {

	Set<Class> classes=new HashSet<Class>();
	
	public CustomEventBus() {
		super();
	}

	
	@Override
	public void register(Object object) {
		
		Class clazz = object.getClass();
		
		if(classes.contains(clazz)) {
			System.out.println("Class already registered on eventbus: "+clazz.getName());
		} else {
			System.out.println("Registering Class on eventbus: "+clazz.getName());
			super.register(object);
			classes.add(clazz);
		}
	}
	@Override
	public void unregister(Object object) {
		classes.remove(object.getClass());
		super.unregister(object);
	}
	
	
}
