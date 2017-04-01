package cm.homeautomation.services.configuration.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.services.configuration.ConfigurationRESTService;

public class ConfigurationRESTServiceTest {
	
	private ConfigurationRESTService configurationRESTService;

	@Before
	public void setup() {
		configurationRESTService = new ConfigurationRESTService();

	}
	
	@Test
	public void testGetAllParameters() throws Exception {
		configurationRESTService.getAllConfigSettings();
	}
	
	@Test
	public void testCreateParameter() throws Exception {
		
	}
	
	@Test
	public void testUpdateExistingParameter() throws Exception {
		
	}
	
	

}
