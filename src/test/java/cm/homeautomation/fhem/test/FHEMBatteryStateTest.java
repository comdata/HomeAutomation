package cm.homeautomation.fhem.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.fhem.BatteryStateResult;
import cm.homeautomation.fhem.BatteryStateResult.BatteryState;
import cm.homeautomation.fhem.FHEMBatteryStateReceiver;

class FHEMBatteryStateTest {

	private EntityManager em = EntityManagerService.getNewManager();

	@Test
	void testBatteryStateOk() {
		em.getTransaction().begin();
		String name = "BatteryTestDevice" + System.currentTimeMillis();
		String topic = "/fhem/" + name + "/battery";
		String messageContent = "100 %";
		FHEMDevice fhemDevice = new FHEMDevice();
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
	void testBatteryStateTopicNull() {
		em.getTransaction().begin();
		String name = "BatteryTestDevice" + System.currentTimeMillis();
		String topic = "/fhem/" + name + "/battery";
		String messageContent = "100 %";
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setName(name);

		em.persist(fhemDevice);
		em.getTransaction().commit();

		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(null, messageContent, fhemDevice);

		assertNull(batteryStateResult);
	}
	
	@Test
	void testBatteryStateTopicNotEndingWithBattery() {
		em.getTransaction().begin();
		String name = "BatteryTestDevice" + System.currentTimeMillis();
		String topic = "/fhem/" + name + "/batterie";
		String messageContent = "100 %";
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setName(name);

		em.persist(fhemDevice);
		em.getTransaction().commit();

		BatteryStateResult batteryStateResult = FHEMBatteryStateReceiver.receive(topic, messageContent, fhemDevice);

		assertNull(batteryStateResult);
	}

	@Test
	void testBatteryStateNotOk() {
		em.getTransaction().begin();
		String name = "BatteryTestDevice" + System.currentTimeMillis();
		String topic = "/fhem/" + name + "/battery";
		String messageContent = "25 %";
		FHEMDevice fhemDevice = new FHEMDevice();
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
