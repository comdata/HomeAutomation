package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.eventbus.Subscribe;

public class EventBusAnnotationInitializer {

	private Map<Class, Object> instances = new HashMap<Class, Object>();

	public EventBusAnnotationInitializer() {
		init();
	}

	private void init() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		Set<Method> resources = reflections.getMethodsAnnotatedWith(Subscribe.class);

		for (Method method : resources) {
			try {
				Class<?> declaringClass = method.getDeclaringClass();
				LogManager.getLogger(this.getClass()).info("Creating class: " + declaringClass.getName());
				Object classInstance = declaringClass.newInstance();

				instances.put(declaringClass, classInstance);
			} catch (InstantiationException | IllegalAccessException e) {
				LogManager.getLogger(this.getClass()).info("Failed creating class");
			}
		}
	}

	public Map<Class, Object> getInstances() {
		return instances;
	}
}
