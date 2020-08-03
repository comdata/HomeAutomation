package cm.homeautomation.entities.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;

class FHEMDeviceTest {

	private FHEMDevice fhemDevice;

	@BeforeEach
	void setup() {
		fhemDevice = new FHEMDevice();
	}

	@Test
	void testConstructor() {
		assertNotNull(fhemDevice);
	}

	@Test
	void testId() {
		Long idToSet = 2L;

		fhemDevice.setId(idToSet);
		assertEquals(idToSet, fhemDevice.getId());
	}

	@Test
	void testDeviceType() throws Exception {
		FHEMDeviceType deviceType = FHEMDeviceType.WINDOW;
		fhemDevice.setDeviceType(deviceType);
		assertEquals(deviceType, fhemDevice.getDeviceType());
	}
	
	@Test
	void testReferencedId() throws Exception {
		Long referencedId=3L;
		fhemDevice.setReferencedId(referencedId);
		
		assertEquals(referencedId, fhemDevice.getReferencedId());
	}
	
	@Test
	void testDeviceName() throws Exception {
		String name="Window";
		fhemDevice.setName(name);
		
		assertEquals(name, fhemDevice.getName());
	}

}
