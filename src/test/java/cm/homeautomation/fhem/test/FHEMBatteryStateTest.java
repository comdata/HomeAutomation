package cm.homeautomation.fhem.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.fhem.BatteryStateResult;
import cm.homeautomation.fhem.FHEMBatteryStateReceiver;
import cm.homeautomation.fhem.FHEMWindowDataReceiver;
import cm.homeautomation.fhem.BatteryStateResult.BatteryState;

class FHEMBatteryStateTest {

	@Test
	void testBatteryStateOk() {
		String topic = "/fhem/BatteryTestDevice/battery";
		String messageContent= "100 %";
		FHEMDevice fhemDevice= new FHEMDevice();
		fhemDevice.setName("BatteryTestDevice");
		
		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);
	
		assertNotNull(batteryStateResult);
		assertTrue(BatteryState.OK.equals(batteryStateResult.getState()));
		assertEquals(batteryStateResult.getFhemDevice(), fhemDevice);
	}
	
	@Test
	void testBatteryStateNotOk() {
		String topic = "/fhem/BatteryTestDevice/battery";
		String messageContent= "25 %";
		FHEMDevice fhemDevice= new FHEMDevice();
		fhemDevice.setName("BatteryTestDevice");
		
		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);
	
		assertNotNull(batteryStateResult);
		assertTrue(BatteryState.NOTOK.equals(batteryStateResult.getState()));
		assertEquals(batteryStateResult.getFhemDevice(), fhemDevice);
	}

}
