package cm.homeautomation.services.base.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cm.homeautomation.services.base.EventBusAnnotationInitializer;

class EventBusAnnotationInitializerTest {

	@Test
	void testEventBusInitialization() throws InterruptedException {
		EventBusAnnotationInitializer eventBusAnnotationInitializer = new EventBusAnnotationInitializer(false);

		Thread.sleep(10000);

		Map<Class<?>, Object> instances = eventBusAnnotationInitializer.getInstances();

		int instancesSize = instances.size();
		Set<Method> awaitedClasses = eventBusAnnotationInitializer.getEventBusClasses();
		int awaitedClassesInt = awaitedClasses.size();

		// System.out.println(instancesSize + " / " + awaitedClassesInt);

		for (Method method : awaitedClasses) {

			Class<?> awaitedClazz = method.getDeclaringClass();
			// System.out.println(awaitedClazz.getName());
			boolean found = false;

			for (Class<?> clazz : instances.keySet()) {
				// System.out.println("instance: " + clazz.getName());
				if (clazz.equals(awaitedClazz)) {
					found = true;
					break;
				}
			}

			assertTrue(found);
//			if (found) {
//				System.out.println("found");
//			} else {
//				System.out.println("missing");
//			}

		}

		assertTrue(instancesSize > 0);

		assertTrue(awaitedClassesInt == instancesSize);
	}

}
