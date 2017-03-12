package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.eventbus.Subscribe;

public class StartupAnnotationInitializer {

	private Map<Class, Object> instances = new HashMap<Class, Object>();

	public StartupAnnotationInitializer() {
		init();
	}

	private void init() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).setScanners(new MethodAnnotationsScanner()));

		// MethodAnnotationsScanner
		Set<Method> resources = reflections.getMethodsAnnotatedWith(AutoCreateInstance.class);

		for (Method method : resources) {
			try {
				Class<?> declaringClass = method.getDeclaringClass();
				Logger.getLogger(this.getClass()).info("Creating class: " + declaringClass.getName());
				Object classInstance = declaringClass.newInstance();

				instances.put(declaringClass, classInstance);
			} catch (InstantiationException | IllegalAccessException e) {
				Logger.getLogger(this.getClass()).info("Failed creating class");
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
