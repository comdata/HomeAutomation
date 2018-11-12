package cm.homeautomation.fhem.test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.fhem.BatteryStateResult;
import cm.homeautomation.fhem.FHEMBatteryStateReceiver;
import cm.homeautomation.fhem.FHEMWindowDataReceiver;
import cm.homeautomation.fhem.BatteryStateResult.BatteryState;

class FHEMBatteryStateTest {

	private EntityManager em=EntityManagerService.getNewManager();
	
	@Test
	void testBatteryStateOk() {
		em.getTransaction().begin();
		String name="BatteryTestDevice"+System.currentTimeMillis();
		String topic = "/fhem/"+name+"/battery";
		String messageContent= "100 %";
		FHEMDevice fhemDevice= new FHEMDevice();
		fhemDevice.setName(name);
		
		em.persist(fhemDevice);
		em.getTransaction().commit();
		
		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);
	
		assertNotNull(batteryStateResult);
		assertTrue(BatteryState.OK.equals(batteryStateResult.getState()));
		assertEquals(batteryStateResult.getFhemDevice(), fhemDevice);
		assertTrue(batteryStateResult.getStateValue().equals(BigDecimal.valueOf(100)));
	}
	
	@Test
	void testBatteryStateNotOk() {
		em.getTransaction().begin();
		String name="BatteryTestDevice"+System.currentTimeMillis();
		String topic = "/fhem/"+name+"/battery";
		String messageContent= "25 %";
		FHEMDevice fhemDevice= new FHEMDevice();
		fhemDevice.setName(name);
		em.persist(fhemDevice);
		em.getTransaction().commit();
		
		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);
	
		assertNotNull(batteryStateResult);
		assertTrue(BatteryState.NOTOK.equals(batteryStateResult.getState()));
		assertEquals(batteryStateResult.getFhemDevice(), fhemDevice);
		assertTrue(batteryStateResult.getStateValue().equals(BigDecimal.valueOf(25)));
	}

}