package cm.homeautomation.logging;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.base.AutoCreateInstance;

/**
 * configure log4j to use a configured file
 *
 * @author christoph
 *
 */
@AutoCreateInstance
public class LogConfigurator {

	public LogConfigurator() {
		init();
	}

	private void init() {
		final String loggingFile = ConfigurationService.getConfigurationProperty("logging", "log4j");

		final LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		final File file = new File(loggingFile);

		// this will force a reconfiguration
		context.setConfigLocation(file.toURI());
	}
}
