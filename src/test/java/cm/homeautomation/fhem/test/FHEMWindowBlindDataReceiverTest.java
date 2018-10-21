package cm.homeautomation.fhem.test;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.FHEMDevice;
import cm.homeautomation.entities.FHEMDevice.FHEMDeviceType;
import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.fhem.FHEMWindowBlindDataReceiver;
import cm.homeautomation.services.base.GenericStatus;

public class FHEMWindowBlindDataReceiverTest {

	@Test
	public void testReceiveSuccessful() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		WindowBlind windowBlind = new WindowBlind();
		windowBlind.setName(""+System.currentTimeMillis());
		em.persist(windowBlind);
		
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setDeviceType(FHEMDeviceType.WINDOWBLIND);
		fhemDevice.setReferencedId(windowBlind.getId());
		fhemDevice.setName(windowBlind.getName());
		
		em.getTransaction().commit();
		
		GenericStatus genericStatus = FHEMWindowBlindDataReceiver.receive("/fhem/"+windowBlind.getName()+"/state","dim 95", fhemDevice);
	
		assertNotNull(genericStatus);
		assertTrue(genericStatus.isSuccess());
	}

	@Test
	public void testReceiveGeneratesNull() {
		EntityManager em = EntityManagerService.getNewManager();
		
		em.getTransaction().begin();
		
		WindowBlind windowBlind = new WindowBlind();
		windowBlind.setName(""+System.currentTimeMillis());
		em.persist(windowBlind);
		
		FHEMDevice fhemDevice = new FHEMDevice();
		fhemDevice.setDeviceType(FHEMDeviceType.WINDOWBLIND);
		fhemDevice.setReferencedId(windowBlind.getId());
		fhemDevice.setName(windowBlind.getName());
		
		em.getTransaction().commit();
		
		GenericStatus genericStatus = FHEMWindowBlindDataReceiver.receive("/fhem/"+windowBlind.getName()+"/nostate","dim 95", fhemDevice);
	
		assertNull(genericStatus);
	}
}
