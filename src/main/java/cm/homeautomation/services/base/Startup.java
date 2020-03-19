
package cm.homeautomation.services.base;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * calls the startup annotation initializer
 * 
 * @author christoph
 *
 */
@ApplicationScoped
public class Startup {

	private StartupAnnotationInitializer startupAnnotationInitializer;

	private static final Logger LOGGER = Logger.getLogger("ListenerBean");

	void onStart(@Observes StartupEvent ev) {
		LOGGER.info("The application is starting...");

		startupAnnotationInitializer = new StartupAnnotationInitializer();
		startupAnnotationInitializer.start();
	}

	void onStop(@Observes ShutdownEvent ev) {
		LOGGER.info("The application is stopping...");
		startupAnnotationInitializer.disposeInstances();
	}

}
