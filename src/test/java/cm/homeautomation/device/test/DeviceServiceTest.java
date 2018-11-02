package cm.homeautomation.device.test;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import cm.homeautomation.device.DeviceService;

class DeviceServiceTest {

	@Test
	public void testDeviceNotFound() {
		assertNull(DeviceService.getDeviceForMac("FF:FF:FF:FF:FF"));
	}
	
	@Test
	public void testRoomNotFound() {
		assertNull(DeviceService.getRoomForMac("FF:FF:FF:FF:FF"));
	}

	@Test
	public void testRoomIdForMacNotFound() {
		assertNull(DeviceService.getRoomIdForMac("FF:FF:FF:FF:FF"));	
	}
	
}
