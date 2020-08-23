package cm.homeautomation.services.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.greenrobot.eventbus.Subscribe;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.base.Predicate;

import cm.homeautomation.configuration.ConfigurationService;

/**
 * initialize event bus annotated objects
 *
 * @author christoph
 *
 */
@AutoCreateInstance
public class EventBusAnnotationInitializer {

	private static Map<Class<?>, Object> instances = new HashMap<>();

	public EventBusAnnotationInitializer() {
		String eventbusEnabled = ConfigurationService.getConfigurationProperty("eventbus", "enable");
		boolean eventbusEnabledBoolean = false;

		if (eventbusEnabled != null && "true".equalsIgnoreCase(eventbusEnabled)) {
			eventbusEnabledBoolean = true;
		}

		if (eventbusEnabledBoolean) {
			initializeEventBus();
		}
	}

	public EventBusAnnotationInitializer(boolean noInit) {
		if (!noInit) {
			initializeEventBus();
		}
	}

	public Map<Class<?>, Object> getInstances() {
		return instances;
	}

	public void initializeEventBus() {
		final Set<Method> resources = getEventBusClasses();

		Map<Class<?>, Object> startupInstances = StartupAnnotationInitializer.getInstances();

		Map<Class<?>, Object> startedInstances = new HashMap<>();

		for (final Method method : resources) {
			final Class<?> declaringClass = method.getDeclaringClass();

			// check if the class has already been initialized
			if (!startedInstances.containsKey(declaringClass) && !instances.containsKey(declaringClass)
					&& !startupInstances.containsKey(declaringClass)) {

				startedInstances.put(declaringClass, "started");

				LogManager.getLogger(this.getClass()).info("Creating class: " + declaringClass.getName());

				final Runnable singleInstanceCreator = new Runnable() {
					@Override
					public void run() {
						try {
							final Object classInstance = declaringClass.newInstance();

							instances.put(declaringClass, classInstance);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| SecurityException e) {
							LogManager.getLogger(this.getClass()).info("Failed creating class");
						}
					}
				};
				new Thread(singleInstanceCreator).start();
			}
		}
	}

	public Set<Method> getEventBusClasses() {
		Predicate<String> filter = new FilterBuilder().includePackage("cm.homeautomation").include(".*class")
				.exclude(".*java").exclude(".*properties").exclude(".*xml");

		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("cm.homeautomation")).filterInputsBy(filter)
				.setScanners(new TypeElementsScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner())
				.useParallelExecutor());

		// MethodAnnotationsScanner
		return reflections.getMethodsAnnotatedWith(Subscribe.class);

	}
}
