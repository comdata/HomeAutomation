package cm.homeautomation.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cm.homeautomation.configuration.ConfigurationService;

/**
 * Created by sadupa on 7/26/14.
 */
public class LogDemo {

	private static final Logger logger = LogManager.getLogger(LogDemo.class.getName());


	public static void main(String[] args) {
		ConfigurationService.createOrUpdate("logging", "log4j", "src/main/java/log4j.xml");
		
		new LogConfigurator();

		logger.debug("Hello world - debug log");
		logger.info("Hello world - info log");
		logger.warn("Hello world - warn log");
		logger.error("Hello world - error log");
	}
}
