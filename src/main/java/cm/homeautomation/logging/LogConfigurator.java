package cm.homeautomation.logging;

import java.io.File;
import java.net.URI;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import cm.homeautomation.configuration.ConfigurationService;

/**
 * configure log4j to use a configured file
 *
 * @author christoph
 *
 */
public class LogConfigurator {

    private long domConfigureAndWatchTime=60L*1000;

	public LogConfigurator() {
		init();
	}

	private void init() {
		final String loggingFile = ConfigurationService.getConfigurationProperty("logging", "log4j");

		final LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        final File file = new File(loggingFile);

		if (file.exists()) {
			// this will force a reconfiguration
			URI log4jURI = file.toURI();
			context.setConfigLocation(log4jURI);
			
			DOMConfigurator.configureAndWatch(loggingFile, domConfigureAndWatchTime);
			
		} else {
			System.out.println();
		}
	}

}
