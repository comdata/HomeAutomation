package cm.homeautomation.services.base.test;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import cm.homeautomation.services.base.StartupAnnotationInitializer;

public class StartupAnnotationInitializerTest {

	@Test
	public void testStartupInitializer() {
		StartupAnnotationInitializer startupAnnotationInitializer = new StartupAnnotationInitializer();
		Set<Class<?>> classesWithAutoCreateInstance = startupAnnotationInitializer.getClassesWithAutoCreateInstance();
		
		for (Class<?> clazz : classesWithAutoCreateInstance) {
			System.out.println(clazz.getName());
		}
		assertTrue(!classesWithAutoCreateInstance.isEmpty());
		
	}

}
