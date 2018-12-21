package cm.homeautomation.services.base.test;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import cm.homeautomation.services.base.EventBusAnnotationInitializer;

public class EventBusAnnotationInitializerTest {

	@Test
	public void testEventBusClassesAreFound() {
		
		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer(true);
		Set<Method> eventBusClasses = eventBusAnnotationInitializer.getEventBusClasses();
	
		for (Method method : eventBusClasses) {
			System.out.println(method.getDeclaringClass().getName());
		}
		
		assertFalse(eventBusClasses.isEmpty());
	}
	
	@Test
	public void testEventBusInitialization() {
		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer();
	
		Map<Class, Object> instances = eventBusAnnotationInitializer.getInstances();
		assertFalse(instances.isEmpty());
	}

}
