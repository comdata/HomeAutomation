package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.eventbus.Subscribe;

public class EventBusAnnotationInitializer {

	private Map<Class, Object> instances=new HashMap<Class, Object>();
	
	public EventBusAnnotationInitializer() {
		init();
	}

	private void init() {
		Reflections reflections = new Reflections("cm.homeautomation");
	
		
		//MethodAnnotationsScanner
		Set<Method> resources =
		    reflections.getMethodsAnnotatedWith(Subscribe.class);
		
		for (Method method : resources) {
			try {
				Class<?> declaringClass = method.getDeclaringClass();
				System.out.println("Creating class: "+declaringClass.getName());
				Object classInstance = declaringClass.newInstance();
				
				instances.put(declaringClass, classInstance);
			} catch (InstantiationException | IllegalAccessException e) {
				System.out.println("Failed creating class");
			}
		}
	}
	
	public Map<Class, Object> getInstances() {
		return instances;
	}
}
