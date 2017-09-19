package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class StartupAnnotationInitializer extends Thread {

	private Map<Class, Object> instances = new HashMap<Class, Object>();

	public StartupAnnotationInitializer() {
	}

	public void run() {
		System.out.println("Scanning classes");
		Reflections reflections = new Reflections(
				new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(
						new SubTypesScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		Set<Method> resources = reflections.getMethodsAnnotatedWith(AutoCreateInstance.class);
		Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(AutoCreateInstance.class);

		for (Method method : resources) {
			Class<?> declaringClass = method.getDeclaringClass();
			String message = "Adding class: " + declaringClass.getName();
			System.out.println(message);
			typesAnnotatedWith.add(declaringClass);
		}

		for (Class<?> declaringClass : typesAnnotatedWith) {

			String message = "Creating class: " + declaringClass.getName();
			System.out.println(message);
			LogManager.getLogger(this.getClass()).info(message);

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

	public Map<Class, Object> getInstances() {
		return instances;
	}

	public void disposeInstances() {
		Set<Class> keySet = instances.keySet();

		for (Class clazz : keySet) {

			Object singleInstance = instances.get(clazz);

			instances.remove(clazz);
		}
	}
}
