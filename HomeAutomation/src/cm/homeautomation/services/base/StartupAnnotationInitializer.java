package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class StartupAnnotationInitializer {

	private Map<Class, Object> instances = new HashMap<Class, Object>();

	public StartupAnnotationInitializer() {
	}

	public void init() {
		System.out.println("Scanning classes");
		Reflections reflectionsMethods = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new MethodAnnotationsScanner()));

		Reflections reflectionsClasses = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new TypeAnnotationsScanner()));
		
		// MethodAnnotationsScanner
		Set<Method> resources = reflectionsMethods.getMethodsAnnotatedWith(AutoCreateInstance.class);
		Set<Class<?>> typesAnnotatedWith = reflectionsClasses.getTypesAnnotatedWith(AutoCreateInstance.class);

		for (Method method : resources) {
			Class<?> declaringClass = method.getDeclaringClass();
			String message = "Adding class: " + declaringClass.getName();
			typesAnnotatedWith.add(declaringClass);
		}
		
		for (Class<?> declaringClass : typesAnnotatedWith) {
			try {
				String message = "Creating class: " + declaringClass.getName();
				System.out.println(message);
				LogManager.getLogger(this.getClass()).info(message);
				
				Object classInstance = declaringClass.newInstance();

				instances.put(declaringClass, classInstance);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				LogManager.getLogger(this.getClass()).info("Failed creating class");
			}
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
