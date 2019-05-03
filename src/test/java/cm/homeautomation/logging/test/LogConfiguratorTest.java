package cm.homeautomation.logging.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.logging.LogConfigurator;

public class LogConfiguratorTest {

	@BeforeEach
	public void setup() {
		ConfigurationService.createOrUpdate("logging", "log4j", "src/main/java/log4j2.xml");
	}
	
	@Test
	public void testLogConfigurator() {
		
		LogConfigurator logConfigurator = new LogConfigurator();
		
		assertNotNull(logConfigurator);
		
		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		assertTrue(context.getConfigLocation().toString().endsWith("log4j2.xml"));
	}
}
