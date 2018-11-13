package cm.homeautomation.fhem.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.fhem.ZWaveNeighborUpdate;

public class ZWaveNeighborUpdateTest {

	@BeforeEach
	public void setup() {
		ConfigurationService.createOrUpdate("zwave", "perDeviceUrl", "http://localhost:3000/");
		
		
	}
	
	@Test
	public void testPerformNeighborUpdate() {
		ZWaveNeighborUpdate.performNeighborUpdate(null);
	}
	
}
