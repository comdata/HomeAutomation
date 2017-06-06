package cm.homeautomation.eventbus;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class CustomEventBus extends AsyncEventBus {

	Set<Class> classes=new HashSet<Class>();
	
	EventBus eventBus;
	
	public CustomEventBus() {
		super(Executors.newCachedThreadPool());
		eventBus=this;
	}

	
	@Override
	public void register(Object object) {
		
		Class clazz = object.getClass();
		
		if(classes.contains(clazz)) {
			System.out.println("Class already registered on eventbus: "+clazz.getName());
		} else {
			System.out.println("Registering Class on eventbus: "+clazz.getName());
			eventBus.register(object);
			classes.add(clazz);
		}
	}
	@Override
	public void unregister(Object object) {
		classes.remove(object.getClass());
		eventBus.unregister(object);
	}
	
	
}
