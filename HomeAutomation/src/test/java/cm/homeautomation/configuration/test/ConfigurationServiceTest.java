package cm.homeautomation.configuration.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.ConfigurationSetting;

public class ConfigurationServiceTest {

	private static final String TEST_VALUE_UPDATE = "testValueUpdate";
	private static final String TEST_GROUP = "testGroup";
	private static final String TEST_PROP = "testProp";
	private static final String TEST_VALUE = "testValue";
	private static final String TEST_GROUP_UNKNOWN = "testGroupUnknown";

	@Test
	public void testUnknownConfigSetting() {

		String configurationProperty = ConfigurationService.getConfigurationProperty(TEST_GROUP_UNKNOWN, TEST_PROP);

		assertTrue(configurationProperty == null);
	}

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

	@Test
	public void testPurgeAllConfigurationSettings() {
		ConfigurationService.createOrUpdate(TEST_GROUP, TEST_PROP, TEST_VALUE);
		ConfigurationService.purgeAllSettings();

		List<ConfigurationSetting> allSettings = ConfigurationService.getAllSettings();

		assertNull("list is NULL", allSettings);

	}

	@Test
	public void testGetAllSettings() throws Exception {
		ConfigurationService.purgeAllSettings();
		ConfigurationService.createOrUpdate(TEST_GROUP, TEST_PROP, TEST_VALUE);
		List<ConfigurationSetting> allSettings = ConfigurationService.getAllSettings();

		assertNotNull("list not NULL", allSettings);
		assertTrue("exactly one entry present", allSettings.size() == 1);

	}
}
