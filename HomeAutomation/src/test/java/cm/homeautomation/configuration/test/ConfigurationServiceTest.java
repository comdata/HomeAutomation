package cm.homeautomation.configuration.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cm.homeautomation.configuration.ConfigurationService;

public class ConfigurationServiceTest {

	private static final String TEST_VALUE_UPDATE = "testValueUpdate";
	private static final String TEST_GROUP = "testGroup";
	private static final String TEST_PROP = "testProp";
	private static final String TEST_VALUE = "testValue";

	@Test
	public void testCreateConfigSetting() {
		ConfigurationService.createOrUpdate(TEST_GROUP, TEST_PROP, TEST_VALUE);
		
		String configurationProperty = ConfigurationService.getConfigurationProperty(TEST_GROUP, TEST_PROP);
	
		assertTrue(TEST_VALUE.equals(configurationProperty));
	}

	@Test
	public void testUpdateConfigSetting() {
		ConfigurationService.createOrUpdate(TEST_GROUP, TEST_PROP, TEST_VALUE);
		ConfigurationService.createOrUpdate(TEST_GROUP, TEST_PROP, TEST_VALUE_UPDATE);
		
		String configurationProperty = ConfigurationService.getConfigurationProperty(TEST_GROUP, TEST_PROP);
	
		assertTrue(TEST_VALUE_UPDATE.equals(configurationProperty));
	}
}
