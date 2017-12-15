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

	private static Map<Class, Object> instances = new HashMap<Class, Object>();

	public EventBusAnnotationInitializer() {
		this.run();
	}

	public void run() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		Set<Method> resources = reflections.getMethodsAnnotatedWith(Subscribe.class);

		for (Method method : resources) {
			Class<?> declaringClass = method.getDeclaringClass();

			// check if the class has already been initialized
			if (!instances.containsKey(declaringClass)) {

				LogManager.getLogger(this.getClass()).info("Creating class: " + declaringClass.getName());

				Runnable singleInstanceCreator = new Runnable() {
					public void run() {
						try {
							Object classInstance = declaringClass.newInstance();

							instances.put(declaringClass, classInstance);
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
							LogManager.getLogger(this.getClass()).info("Failed creating class");
						}
					}
				};
				new Thread(singleInstanceCreator).start();
			}
		}
	}

	public Map<Class, Object> getInstances() {
		return instances;
	}
}
