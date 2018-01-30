package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@AutoCreateInstance
public class EventBusAnnotationInitializer extends Thread {

	private static Map<Class, Object> instances = new HashMap<>();

	public EventBusAnnotationInitializer() {
		this.run();
	}

	public Map<Class, Object> getInstances() {
		return instances;
	}

	@Override
	public void run() {
		final Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		final Set<Method> resources = reflections.getMethodsAnnotatedWith(Subscribe.class);

		for (final Method method : resources) {
			final Class<?> declaringClass = method.getDeclaringClass();

			// check if the class has already been initialized
			if (!instances.containsKey(declaringClass)) {

				LogManager.getLogger(this.getClass()).info("Creating class: " + declaringClass.getName());

				final Runnable singleInstanceCreator = new Runnable() {
					@Override
					public void run() {
						try {
							final Object classInstance = declaringClass.newInstance();

							instances.put(declaringClass, classInstance);
						} catch (InstantiationException | IllegalAccessException e) {
							LogManager.getLogger(this.getClass()).info("Failed creating class");
						}
					}
				};
				new Thread(singleInstanceCreator).start();
			}
		}
	}
}
